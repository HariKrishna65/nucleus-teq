import pytest
import jwt

from backend.utils.security import create_access_token, decode_access_token


def test_create_and_decode_access_token():
    token = create_access_token(
        user_id="user-123",
        email="patient@example.com",
        role="PATIENT",
    )

    payload = decode_access_token(token)

    assert payload.sub == "user-123"
    assert payload.email == "patient@example.com"
    assert payload.role == "PATIENT"
    assert payload.exp > payload.iat


def test_decode_invalid_token_raises_error():
    with pytest.raises(jwt.InvalidTokenError):
        decode_access_token("invalid-token")
