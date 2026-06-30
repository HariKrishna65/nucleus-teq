from backend.database import build_mongodb_uri, get_database_name


def test_build_mongodb_uri_from_environment(monkeypatch):
    monkeypatch.setenv("MONGODB_USERNAME", "demo_user")
    monkeypatch.setenv("MONGODB_PASSWORD", "demo_pass")
    monkeypatch.setenv("MONGODB_HOST", "cluster0.mongodb.net")
    monkeypatch.setenv("MONGODB_DB_NAME", "doctor_appointment")
    monkeypatch.setenv("MONGODB_USE_SRV", "true")

    uri = build_mongodb_uri()
    assert uri == "mongodb+srv://demo_user:demo_pass@cluster0.mongodb.net/?retryWrites=true&w=majority"
    assert get_database_name() == "doctor_appointment"


def test_build_mongodb_uri_uses_direct_override(monkeypatch):
    monkeypatch.setenv("MONGODB_URI", "mongodb+srv://real-user:real-pass@cluster0.mongodb.net/?retryWrites=true&w=majority")

    assert build_mongodb_uri() == "mongodb+srv://real-user:real-pass@cluster0.mongodb.net/?retryWrites=true&w=majority"
