import pytest

from backend.services import appointment_service as svc


def test_create_and_list_slot():
    # clear local storage
    svc.slots_local.clear()

    slot = svc.create_slot({"doctor_id": "doc1@example.com", "date": "2026-07-10", "time": "10:00"})
    assert slot["doctor_id"] == "doc1@example.com"
    slots = svc.list_slots(doctor_id="doc1@example.com")
    assert any(s["id"] == slot["id"] for s in slots)


def test_book_appointment_and_prevent_double_book():
    svc.slots_local.clear()
    svc.appointments_local.clear()

    svc.create_slot({"doctor_id": "doc2@example.com", "date": "2026-07-11", "time": "11:00"})

    appt = svc.create_appointment({
        "doctor_id": "doc2@example.com",
        "appointment_date": "2026-07-11",
        "slot_time": "11:00",
        "patient_email": "patient1@example.com",
    })
    assert appt["status"] == "CONFIRMED"

    # Trying to book the same slot again should fail
    with pytest.raises(ValueError):
        svc.create_appointment({
            "doctor_id": "doc2@example.com",
            "appointment_date": "2026-07-11",
            "slot_time": "11:00",
            "patient_email": "patient2@example.com",
        })


def test_cancel_appointment_frees_slot():
    svc.slots_local.clear()
    svc.appointments_local.clear()

    svc.create_slot({"doctor_id": "doc3@example.com", "date": "2026-07-12", "time": "12:00"})

    appt = svc.create_appointment({
        "doctor_id": "doc3@example.com",
        "appointment_date": "2026-07-12",
        "slot_time": "12:00",
        "patient_email": "patient3@example.com",
    })

    appt_id = appt["id"]
    cancelled = svc.cancel_appointment(appt_id, requester_email="patient3@example.com")
    assert cancelled["status"] == "CANCELLED"

    slots = svc.list_slots(doctor_id="doc3@example.com", date="2026-07-12")
    assert slots and slots[0]["booked"] is False
