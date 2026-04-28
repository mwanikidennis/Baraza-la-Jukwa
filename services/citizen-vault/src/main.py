import os
import io
import uuid
import requests
from fastapi import FastAPI, UploadFile, File, Form
from cryptography.fernet import Fernet
from PIL import Image
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="Jukwa Citizen Vault Relay")

# NGO-Aligned Vault Encryption Key (Mocked)
# In production, this would be managed by a trusted HSM or NGO security lead
VAULT_KEY = os.getenv("VAULT_KEY", Fernet.generate_key().decode())
fernet = Fernet(VAULT_KEY.encode())

TOR_PROXY = "http://tor:9050" # SOCKS5 proxy in the docker network

@app.get("/health")
async def health():
    return {"status": "UP", "service": "citizen-vault-relay"}

@app.post("/relay")
async def process_whistleblower_upload(
    incident_details: str = Form(...),
    file: UploadFile = File(...)
):
    """
    Acts as the independent 'Citizen Vault' relay.
    1. Securely backup raw evidence.
    2. Scrub metadata.
    3. Route through Tor to the main Jukwa API.
    """
    raw_data = await file.read()
    
    # STEP 1: Securely backup to the 'NGO Vault' (Encrypted)
    # Why: Ensures evidence is immutable and protected from state deletion.
    vault_id = str(uuid.uuid4())
    encrypted_data = fernet.encrypt(raw_data)
    
    os.makedirs("../backups", exist_ok=True)
    with open(f"../backups/{vault_id}.vault", "wb") as f:
        f.write(encrypted_data)

    # STEP 2: Metadata Scrubbing
    # Why: Destroys GPS tags, device signatures, and EXIF data.
    sanitized_io = io.BytesIO()
    if file.content_type.startswith("image/"):
        img = Image.open(io.BytesIO(raw_data))
        # Strip EXIF by saving to new BytesIO without parameters
        img.save(sanitized_io, format=img.format)
        sanitized_data = sanitized_io.getvalue()
    else:
        # Fallback for non-image files (needs more complex byte scrubbing for PDF/Video)
        sanitized_data = raw_data 

    # STEP 3: Route through Tor Network
    # Why: IP anonymity. The State API receives the report but sees a Tor exit node.
    proxies = {
        'http': 'socks5h://jukwa-tor-proxy:9050',
        'https': 'socks5h://jukwa-tor-proxy:9050'
    }
    
    # Target is the internal Docker address of the incident-service
    # But routed via Tor proxy to demonstrate the concept
    target_api = "http://incident-service:3001/incidents"
    
    try:
        # In a real scenario, we would use a hidden service (.onion) address
        response = requests.post(
            target_api,
            files={'media': (file.filename, sanitized_data, file.content_type)},
            data={'incident_text': incident_details, 'citizen_id': 'ANONYMOUS_WHISTLEBLOWER'},
            proxies=proxies,
            timeout=30
        )
        
        return {
            "status": "SUCCESS",
            "vault_receipt": vault_id,
            "message": "Evidence secured and routed anonymously.",
            "upstream_response": response.status_code
        }
    except Exception as e:
        return {"status": "FAILED", "error": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=3011)
