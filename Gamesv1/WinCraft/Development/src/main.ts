import './style.css';
import { GSWebSocketClient, type GSConfig } from './network/GSWebSocketClient';
import { SlotEngine } from './game/SlotEngine';
import { UIManager } from './ui/UIManager';
import { AudioManager } from './audio/AudioManager';

document.querySelector<HTMLDivElement>('#app')!.innerHTML = `
  <div class="game-layout">
    <!-- TOP: JACKPOT / NOTIFICATION BAR -->
    <div class="top-bar">
      <div class="jackpot-ticker">GRAND: $10,000.00 | MAJOR: $5,000.00 | MINOR: $500.00</div>
    </div>

    <!-- MIDDLE: THE ACTUAL GAME (PIXIJS) -->
    <div class="game-view">
        <!-- CELEBRATION OVERLAY -->
        <div id="win-celebration" class="win-celebration" style="display: none;">
            <h1 id="win-type-text">BIG WIN!</h1>
            <h2 id="win-amount-text">$0.00</h2>
        </div>
        <div id="pixi-container"></div>
        <button class="btn-buy-bonus" id="btn-buy-bonus" title="Buy the Ender Bonus Feature"></button>
    </div>

    <!-- BOTTOM: PREMIUM GLASSMORPHISM HUD -->
    <div class="bottom-hud">
      <!-- LEFT: Controls -->
      <div class="hud-left">
        <button class="hud-btn" id="btn-home" title="Lobby/Home">
            <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
        </button>
        <button class="hud-btn" id="btn-sound" title="Sound">
            <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path></svg>
        </button>
        <button class="hud-btn" id="btn-paytable" title="Paytable">
            <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
        </button>
      </div>
      
      <!-- CENTER: Balances box -->
      <div class="hud-center">
        <div class="hud-panel balance-panel">
          <span class="hud-label">BALANCE <span id="lbl-frb" style="display:none; color: #f0f;"></span></span>
          <span class="hud-value" id="lbl-balance">$0.00</span>
        </div>
        
        <div class="hud-panel win-panel">
          <span class="hud-label">WIN</span>
          <span class="hud-value highlight" id="lbl-win">$0.00</span>
        </div>

        <div class="hud-panel bet-panel">
          <span class="hud-label">BET</span>
          <div class="bet-controls-wrapper">
            <button class="bet-btn" id="btn-bet-down">-</button>
            <span class="hud-value" id="lbl-bet">$1.00</span>
            <button class="bet-btn" id="btn-bet-up">+</button>
          </div>
        </div>
      </div>

      <!-- RIGHT: Spin Action -->
      <div class="hud-right">
        <button class="hud-btn feature-btn" id="btn-auto" title="Autoplay"> AUTO </button>
        <button class="btn-spin-premium" id="btn-spin" disabled>
            <div class="spin-icon">↻</div>
        </button>
        <button class="hud-btn feature-btn" id="btn-turbo" title="Turbo"> TURBO </button>
      </div>
    </div>

    <!-- CERTIFICATION HUD (Bottom Compliance Info) -->
    <div class="cert-hud">
        <span id="cert-time">12:00 PM</span>
        <span id="cert-session">Session: 00:00:00</span>
        <span id="cert-loss">Net: $0.00</span>
    </div>

    <!-- PAYTABLE / RULES MODAL (Hidden by default) -->
    <div id="modal-paytable" class="modal-overlay" style="display: none;">
        <div class="modal-content">
            <button class="btn-close" id="btn-close-paytable">X</button>
            <h2>PAYTABLE & RULES</h2>
            <div class="paytable-grid">
               <!-- Dummy content, will be populated by game config later -->
               <div class="pay-item"><span>Sym 5 (Wild)</span><span>3x: 50.00</span></div>
               <div class="pay-item"><span>Sym 4 (High)</span><span>3x: 20.00</span></div>
               <div class="pay-item"><span>Sym 3 (Mid)</span><span>3x: 10.00</span></div>
               <div class="pay-item"><span>Sym 2 (Low)</span><span>3x: 5.00</span></div>
               <div class="pay-item"><span>Sym 1 (Low)</span><span>3x: 2.00</span></div>
            </div>
            <p class="rtp-info">Theoretical RTP: 96.50%</p>
        </div>
    </div>
  </div>

  <!-- RIGHT: DEBUGGER PANEL (Hidden in production) -->
  <div class="debugger-panel card">
    <h3>GS Abs.v1 Tools</h3>
    <p id="status">Platform Status: 🔴 Disconnected</p>
    <p id="engine-status">Engine State: INIT</p>
    <button id="btn-connect" style="margin-bottom: 15px; display: none;">1. Connect GS</button>
    
    <div id="logs" class="debugger-logs"></div>
  </div>
`

// Debugger DOM
const statusEl = document.querySelector<HTMLParagraphElement>('#status')!;
const logsEl = document.querySelector<HTMLDivElement>('#logs')!;
const btnConnect = document.querySelector<HTMLButtonElement>('#btn-connect')!;

const log = (msg: string) => {
  const p = document.createElement('div');
  p.innerText = `[${new Date().toLocaleTimeString()}] ${msg}`;
  logsEl.appendChild(p);
  logsEl.scrollTop = logsEl.scrollHeight;
};

// Override standard console out so it surfaces in the UI widget
const oldLog = console.log;
console.log = function (...args) {
  log(args.join(' '));
  oldLog.apply(console, args);
}
const oldWarn = console.warn;
console.warn = function (...args) {
  log("⚠️ " + args.join(' '));
  oldWarn.apply(console, args);
}

// Mock Launch Configuration
const mockConfig: GSConfig = {
  wssUrl: "ws://localhost:6001",
  authToken: "mock-jwt-token-123",
  sessionId: "sess-9999",
  bankId: "bank-1",
  gameId: "slot-template-01"
};

// Architecture Wiring
const networkClient = new GSWebSocketClient(mockConfig);
const engine = new SlotEngine(networkClient);
const uiManager = new UIManager(engine); // Captures #pixi-container

// --- Application Boot Sequence & Loading Screen ---
const loadScreen = document.getElementById('loading-screen') as HTMLDivElement;
const progContainer = document.getElementById('loading-progress-container') as HTMLDivElement;
const loadText = document.getElementById('loading-text') as HTMLParagraphElement;
const btnStartGame = document.getElementById('btn-start-game') as HTMLButtonElement;

async function bootstrapGame() {
  let progress = 0;

  // 1. Simulate Asset Loading Progression (while waiting for Pixi)
  const loadInterval = setInterval(() => {
    progress += Math.random() * 15;
    if (progress > 95) progress = 95;
    progContainer.style.setProperty('--fill', `${progress}%`);
  }, 200);

  try {
    // 2. Initialize the modern asynchronous PixiJS V8 canvas
    await uiManager.initPixi();
    (window as any).uiManager = uiManager;
    log("🎨 PixiJS UI Graphics successfully initialized.");

    // 3. Mark loading 100% Complete
    clearInterval(loadInterval);
    progContainer.style.setProperty('--fill', '100%');
    loadText.innerText = 'Assets Loaded. Ready to play!';

    // 4. Reveal Compliance "TAP TO START" Button
    setTimeout(() => {
      btnStartGame.style.display = 'inline-block';
      loadText.style.display = 'none';
    }, 500);

  } catch (err) {
    clearInterval(loadInterval);
    log(`🚨 UI Init Error: ${err}`);
    loadText.innerText = 'Fatal Initialisation Error.';
    loadText.style.color = 'red';
  }
}

// 5. Handle user entry gesture
btnStartGame.addEventListener('click', () => {
  // Initialize Audio on user gesture (browser autoplay policy)
  AudioManager.getInstance().init();

  // Hide loading screen smoothly
  loadScreen.style.opacity = '0';
  setTimeout(() => {
    loadScreen.style.display = 'none';

    // Automatically connect to the GS Backend when they enter the game!
    log("User matched Tap to Start. Auto-initiating WS connection...");
    networkClient.connect();
  }, 500);
});

// Sound mute button
const btnSound = document.getElementById('btn-sound') as HTMLButtonElement;
if (btnSound) {
  btnSound.addEventListener('click', () => {
    const muted = AudioManager.getInstance().toggleMute();
    // Swap icon
    const svg = btnSound.querySelector('svg');
    if (svg) {
      if (muted) {
        svg.innerHTML = '<polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><line x1="23" y1="9" x2="17" y2="15"></line><line x1="17" y1="9" x2="23" y2="15"></line>';
        btnSound.style.opacity = '0.5';
      } else {
        svg.innerHTML = '<polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>';
        btnSound.style.opacity = '1';
      }
    }
    log(`🔊 Sound ${muted ? 'muted' : 'unmuted'}`);
  });
}

// Kickoff
bootstrapGame();

// Setup network triggers
// CRITICAL: Preserve the SlotEngine's internal listeners so we don't sever the brain!
const engineOnReady = networkClient.onReady;
networkClient.onReady = () => {
  engineOnReady();
  statusEl.innerText = 'Platform Status: 🟢 Connected';
  btnConnect.disabled = true;
};

const engineOnDisconnect = networkClient.onDisconnect;
networkClient.onDisconnect = () => {
  engineOnDisconnect();
  statusEl.innerHTML = 'Platform Status: 🔴 Disconnected';
  btnConnect.disabled = false;
};

// Launch Game connection
btnConnect.addEventListener('click', () => {
  log("Initiating WS connection...");
  networkClient.connect();
});

// Debug Panel Toggle (Backtick key)
document.addEventListener('keydown', (e) => {
  if (e.key === '`') {
    const panel = document.querySelector<HTMLDivElement>('.debugger-panel');
    if (panel) {
      panel.style.display = panel.style.display === 'none' || panel.style.display === '' ? 'flex' : 'none';
    }
  }
});
