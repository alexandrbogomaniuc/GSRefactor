# Cassandra Table Name Issue - Final Solution

The error "unconfigured columnfamily banks" suggests:
1. The table might have

 a different name (maybe `bank` not `banks`)
2. Or we need to access it differently

## Quick Fix - Try Interactive Mode

Since the `-e` flag isn't working well, let's go interactive:

```bash
docker exec -it gp3-c1-1 cqlsh
```

Then **manually type** (don't copy-paste):

```cql
USE RCasinoKS;
DESCRIBE TABLES;
```

This will show you all tables. Look for `banks` or `bank` or similar.

Then try the UPDATE with the correct table name.

---

## Alternative: Check from Game Provider

Since we got the game server running before, the tables must exist. Let me check the Game Provider's configuration code to see what table it's using...

The Game Provider has been successfully querying banks before (we saw 8 games configured), so the table definitely exists - we just need the right name!

Can you run the interactive cqlsh and share what `DESCRIBE TABLES;` shows?
