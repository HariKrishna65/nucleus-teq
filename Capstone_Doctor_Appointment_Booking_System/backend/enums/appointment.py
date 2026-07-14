from enum import Enum


class AppointmentStatus(str, Enum):
    PENDING_PAYMENT = "PENDING_PAYMENT"
    BOOKED = "BOOKED"
    COMPLETED = "COMPLETED"
    MISSED_APPOINTMENT = "MISSED_APPOINTMENT"
    CANCELLED = "CANCELLED"


class PaymentMethod(str, Enum):
    CARD = "CARD"
    UPI = "UPI"
    CASH = "CASH"


class PaymentStatus(str, Enum):
    PENDING = "PENDING"
    PAID = "PAID"
    SUCCESS = "SUCCESS"


class CancellationRequestStatus(str, Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"
