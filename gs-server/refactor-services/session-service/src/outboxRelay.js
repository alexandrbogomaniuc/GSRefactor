const { Kafka, logLevel } = require('kafkajs');

const OUTBOX_POLL_MS = Number(process.env.SESSION_SERVICE_OUTBOX_RELAY_POLL_MS || 2000);
const OUTBOX_RELAY_ENABLED = String(process.env.SESSION_SERVICE_OUTBOX_RELAY_ENABLED || 'false').toLowerCase() === 'true';
const OUTBOX_TOPIC = process.env.SESSION_SERVICE_OUTBOX_TOPIC || 'abs.session.events.v1';
const OUTBOX_DLQ_TOPIC = process.env.SESSION_SERVICE_OUTBOX_DLQ_TOPIC || 'abs.session.events.dlq.v1';
const OUTBOX_MAX_ATTEMPTS = Number(process.env.SESSION_SERVICE_OUTBOX_MAX_ATTEMPTS || 5);
const OUTBOX_RETRY_BASE_MS = Number(process.env.SESSION_SERVICE_OUTBOX_RETRY_BASE_MS || 1000);
const OUTBOX_BATCH_LIMIT = Number(process.env.SESSION_SERVICE_OUTBOX_BATCH_LIMIT || 100);
const KAFKA_BROKERS = String(process.env.SESSION_SERVICE_KAFKA_BROKERS || '')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);
const CLIENT_ID = process.env.SESSION_SERVICE_KAFKA_CLIENT_ID || 'session-service-outbox-relay';

function eventKey(event) {
  const payload = event.payload || {};
  const bankId = payload.bankId || 'unknown-bank';
  const sessionId = payload.sessionId || event.eventId;
  return `${bankId}:${sessionId}:${event.eventType}`;
}

function eventValue(event) {
  return JSON.stringify({
    eventId: event.eventId,
    eventType: event.eventType,
    createdAt: event.createdAt,
    payload: event.payload || {}
  });
}

function dlqValue(event, deliveryState) {
  return JSON.stringify({
    failedAt: new Date().toISOString(),
    sourceTopic: OUTBOX_TOPIC,
    failedEvent: {
      eventId: event.eventId,
      eventType: event.eventType,
      createdAt: event.createdAt,
      payload: event.payload || {}
    },
    delivery: {
      attempts: deliveryState.attempts,
      lastError: deliveryState.lastError,
      dlqAt: deliveryState.dlqAt
    }
  });
}

async function startOutboxRelay(store) {
  if (!OUTBOX_RELAY_ENABLED) {
    // eslint-disable-next-line no-console
    console.log('session-service outbox relay disabled');
    return async () => {};
  }

  if (KAFKA_BROKERS.length === 0) {
    // eslint-disable-next-line no-console
    console.warn('session-service outbox relay enabled but no Kafka brokers configured');
    return async () => {};
  }

  const kafka = new Kafka({
    clientId: CLIENT_ID,
    brokers: KAFKA_BROKERS,
    logLevel: logLevel.NOTHING
  });

  const producer = kafka.producer();
  await producer.connect();

  let inFlight = false;
  const poll = async () => {
    if (inFlight) {
      return;
    }
    inFlight = true;
    try {
      const pending = store.claimOutboxForDelivery(OUTBOX_BATCH_LIMIT).outbox;
      if (!pending.length) {
        return;
      }

      for (const event of pending) {
        try {
          await producer.send({
            topic: OUTBOX_TOPIC,
            messages: [
              {
                key: eventKey(event),
                value: eventValue(event)
              }
            ]
          });
          store.ackOutbox(event.eventId);
        } catch (error) {
          const failure = store.failOutboxDelivery(
            event.eventId,
            error.message,
            OUTBOX_MAX_ATTEMPTS,
            OUTBOX_RETRY_BASE_MS
          );

          if (!failure.ok) {
            // eslint-disable-next-line no-console
            console.error(`session-service outbox relay failed to mark delivery failure: ${event.eventId}`);
            continue;
          }

          if (failure.movedToDlq) {
            try {
              await producer.send({
                topic: OUTBOX_DLQ_TOPIC,
                messages: [
                  {
                    key: eventKey(event),
                    value: dlqValue(event, failure.outboxEvent)
                  }
                ]
              });
            } catch (dlqError) {
              // eslint-disable-next-line no-console
              console.error(`session-service outbox relay failed to publish DLQ event: ${dlqError.message}`);
            }

            // eslint-disable-next-line no-console
            console.warn(
              `session-service outbox moved to DLQ eventId=${event.eventId} attempts=${failure.outboxEvent.attempts}`
            );
          }
        }
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(`session-service outbox relay error: ${error.message}`);
    } finally {
      inFlight = false;
    }
  };

  const timer = setInterval(() => {
    poll();
  }, OUTBOX_POLL_MS);

  await poll();
  // eslint-disable-next-line no-console
  console.log(
    `session-service outbox relay started topic=${OUTBOX_TOPIC} dlq=${OUTBOX_DLQ_TOPIC} brokers=${KAFKA_BROKERS.join(',')} pollMs=${OUTBOX_POLL_MS} attempts=${OUTBOX_MAX_ATTEMPTS}`
  );

  return async () => {
    clearInterval(timer);
    await producer.disconnect();
  };
}

module.exports = {
  startOutboxRelay
};
