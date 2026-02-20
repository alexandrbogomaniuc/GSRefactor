# Stats Backend Service

This is a FastAPI application that serves Game Lists and Statistics from Cassandra.

## Prerequisites
- Python 3.12+
- Cassandra Database (running on localhost:9042)

## Installation
```bash
pip install -r requirements.txt
```

## Running the Server
```bash
python main.py
```
Or with auto-reload:
```bash
uvicorn main:app --reload
```

## API Documentation
Once running, visit:
[http://localhost:8000/docs](http://localhost:8000/docs)
