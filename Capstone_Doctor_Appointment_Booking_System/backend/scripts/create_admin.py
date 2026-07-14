from argparse import ArgumentParser
from uuid import uuid4

from backend.enums.user import ApprovalStatus, UserRole
from backend.database.database import get_users, save_users
from backend.services.user_service import get_user_by_email, hash_password


def build_parser():
    parser = ArgumentParser(description="Create a local admin user for the doctor appointment system.")
    parser.add_argument("--name", required=True, help="Admin full name")
    parser.add_argument("--email", required=True, help="Admin email address")
    parser.add_argument("--password", required=True, help="Admin password")
    parser.add_argument("--phone", required=True, help="10 digit admin phone number")
    return parser


def main():
    args = build_parser().parse_args()
    if get_user_by_email(args.email):
        raise SystemExit("An account with that email already exists.")
    users = get_users()
    users.append(
        {
            "id": str(uuid4()),
            "name": args.name.strip(),
            "email": args.email.strip().lower(),
            "phone": args.phone.strip(),
            "role": UserRole.ADMIN.value,
            "gender": None,
            "date_of_birth": None,
            "qualification": None,
            "specialization": None,
            "experience": None,
            "license_number": None,
            "consultation_fee": None,
            "clinic_address": None,
            "active": True,
            "approval_status": ApprovalStatus.APPROVED.value,
            "hashed_password": hash_password(args.password),
        }
    )
    save_users(users)
    print(f"Admin created for {args.email.strip().lower()}")


if __name__ == "__main__":
    main()
