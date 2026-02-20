# 🎯 CASSANDRA COMMANDS - Copy/Paste These

You have `cqlsh>` open. **Copy and paste these commands one by one:**

---

## 1. See available keyspaces:
```sql
DESCRIBE KEYSPACES;
```

---

## 2. Check tables in RCasinoSCKS:
```sql
USE RCasinoSCKS;
DESCRIBE TABLES;
```

**Expected:** You should see `bankinfocf` in the list

---

## 3. See the structure of bankinfocf table:
```sql
DESC TABLE bankinfocf;
```

**Look for columns like:** `key`, `jcn`, `scn` (JSON/serialized data)

---

## 4. Check what banks exist:
```sql
SELECT key FROM bankinfocf;
```

**Question:** Do you see 6274 or 6275 in the list?

---

## 5A. If banks 6274/6275 DON'T EXIST:

The bank data is serialized (`scn` = serialized column). We can't easily create it via CQL.

**Solution:** Banks must be created via the Game Provider admin interface or imported.

---

## 5B. If banks 6274/6275 DO EXIST:

Check their current data:
```sql
SELECT * FROM bankinfocf WHERE key IN (6274, 6275);
```

The wallet URLs are inside the serialized `scn` column - **we can't update them directly via SQL!**

---

## ⚠️ PROBLEM DISCOVERED

Bank configuration in Cassandra uses **Java object serialization** (`scn` blob column).
This means we **CANNOT** update wallet URLs using SQL commands!

---

## ✅ SOLUTION: Use BankInfoCache.xml

Since we can't update Cassandra directly, we need to use the XML approach differently:

**The XML file IS the configuration source!** When Game Provider starts, it:
1. Reads `BankInfoCache.xml`
2. Loads it into memory
3. May sync to Cassandra

**Next steps:**
1. I'll update the XML file with banks 6274 and 6275
2. Rebuild the game-server module
3. Restart the container

Type `exit` to quit cqlsh and let me proceed.
