#!/usr/bin/env python3
import urllib.request
import urllib.parse
import http.cookiejar
import re
import sys
import time

base_url = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
install_url = f"{base_url}/install"
db_server = sys.argv[2] if len(sys.argv) > 2 else "localhost,1433"

cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))

print(f"🚀 Checking nopCommerce installation status at {install_url}...")

max_retries = 30
for attempt in range(1, max_retries + 1):
    try:
        req = urllib.request.Request(install_url, headers={"User-Agent": "CI-Installer"})
        with opener.open(req, timeout=15) as resp:
            html = resp.read().decode('utf-8', errors='ignore')
            if "Installation" not in html and "Restart" not in html and "Admin email" not in html:
                print("✅ nopCommerce is already installed or home page is active.")
                sys.exit(0)
            
            token_match = re.search(r'name="__RequestVerificationToken"\s+type="hidden"\s+value="([^"]+)"', html)
            if not token_match:
                token_match = re.search(r'value="([^"]+)"\s+name="__RequestVerificationToken"', html)
            token = token_match.group(1) if token_match else ""
            
            print(f"⏳ Extracted AntiForgeryToken. Sending auto-install payload (db: {db_server})...")
            
            payload = urllib.parse.urlencode({
                "AdminEmail": "admin@yourstore.com",
                "AdminPassword": "Test@123456!",
                "ConfirmPassword": "Test@123456!",
                "DataProvider": "sqlserver",
                "SqlAuthenticationType": "sqlserver",
                "SqlServerName": db_server,
                "SqlDatabaseName": "NopCommerce",
                "SqlServerUsername": "sa",
                "SqlServerPassword": "Test@123456!",
                "SqlServerCreateDatabase": "true",
                "InstallSampleData": "true",
                "__RequestVerificationToken": token
            }).encode('utf-8')
            
            post_req = urllib.request.Request(install_url, data=payload, headers={
                "User-Agent": "CI-Installer",
                "Content-Type": "application/x-www-form-urlencoded"
            })
            
            with opener.open(post_req, timeout=180) as post_resp:
                print("🎉 nopCommerce installation completed successfully!")
                sys.exit(0)
    except Exception as e:
        print(f"Attempt {attempt}/{max_retries}: Waiting for nopCommerce service... ({e})")
        time.sleep(5)

print("❌ Installation timed out.")
sys.exit(1)
