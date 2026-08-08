# 📦 GitHub Actions CI/CD — Tài Liệu Hướng Dẫn

> **File thực tế:** `.github/workflows/nopcommerce-tests.yml`
> **Tech stack:** Maven · Java 17 · TestNG · Selenium Grid · Allure · GitHub Services

---

## 📋 Mục Lục

1. [Pipeline hoạt động thế nào?](#1-pipeline-hoạt-động)
2. [Cấu trúc tổng quan workflow](#2-cấu-trúc-tổng-quan)
3. [Trigger — Khi nào pipeline tự chạy?](#3-trigger)
4. [Job 1 — BUILD](#4-job-1--build)
5. [Job 2 — TEST (3 jobs song song)](#5-job-2--test-parallel)
6. [Job 3 — REPORT (Allure GitHub Pages)](#6-job-3--report)
8. [Biến môi trường — GitHub Secrets](#8-github-secrets)
9. [Parallel Testing với TestNG](#9-parallel-testing)
10. [Thiết lập GitHub trước khi Push](#10-thiết-lập-github)
11. [Cheat Sheet — Lệnh nhanh](#11-cheat-sheet)

---

## 1. Pipeline Hoạt Động

git push → GitHub đọc .github/workflows/*.yml → Tự động chạy Workflow

┌────────────────────────────────────────────────────────┐
│                    WORKFLOW                            │
│                                                        │
│  Job 1          Jobs 2 (parallel)       Job 3          │
│  ┌────────┐    ┌───────────────────┐  ┌────────────┐   │
│  │ build  │ →  │ test-login        │→ │  allure-   │   │
│  │        │    │ test-reg-faker    │  │  report    │   │
│  └────────┘    │ test-reg-millis   │  └────────────┘   │
│                └───────────────────┘                   │
│                ↑ 3 job CHẠY CÙNG LÚC                   │
└────────────────────────────────────────────────────────┘

## 2. Cấu Trúc Tổng Quan

```yaml
name: NopCommerce CI/CD Tests   # Tên workflow (hiện trên GitHub UI)

on:                              # TRIGGER: khi nào chạy
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:             # Cho phép bấm nút chạy thủ công

permissions:                     # Cấp quyền cho GitHub Pages
  pages: write
  id-token: write

jobs:                            # Các công việc cần làm
  build: ...                     # Job 1
  test-login: ...                # Job 2a  ┐ chạy cùng lúc
  test-register-faker: ...       # Job 2b  ┤ (parallel)
  test-register-millis: ...      # Job 2c  ┘
  allure-report: ...             # Job 3
```

---

## 3. Trigger — Khi Nào Pipeline Tự Chạy?


on:
  push:
    branches: [ main, develop ]  # Push code lên nhánh main hoặc develop
  pull_request:
    branches: [ main ]            # Tạo Pull Request về main
  workflow_dispatch:              # Bấm nút chạy thủ công trên GitHub UI


**Hiểu đơn giản:**

| Hành động | Pipeline có chạy? |
|-----------|------------------|
| `git push origin main` | ✅ Có |
| `git push origin feature/login` | ❌ Không (không trong branches list) |
| Tạo PR từ feature → main | ✅ Có |
| Bấm nút "Run workflow" trên GitHub | ✅ Có |

---

## 4. Job 1 — BUILD

```yaml
build:
  name: "🔨 Build"
  runs-on: ubuntu-latest         # Dùng máy ảo Ubuntu mới nhất của GitHub

  steps:
    # Bước 1: Tải code về
    - name: Checkout source code
      uses: actions/checkout@v4  # Action có sẵn của GitHub

    # Bước 2: Cài Java 17
    - name: Set up Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    # Bước 3: Cache Maven (tránh tải lại 200MB mỗi lần)
    - name: Cache Maven packages
      uses: actions/cache@v4
      with:
        path: ~/.m2/repository
        key: maven-${{ runner.os }}-${{ hashFiles('**/pom.xml') }}

    # Bước 4: Compile
    - name: Compile project
      run: mvn clean compile -q
```

**Giải thích từ khóa:**

| Keyword | Ý nghĩa |
|---------|---------|
| `runs-on` | Loại máy ảo để chạy job (ubuntu-latest = Ubuntu mới nhất) |
| `steps` | Danh sách các bước chạy tuần tự |
| `uses` | Dùng "Action" có sẵn (như plugin) |
| `actions/checkout@v4` | Action tải code về máy ảo |
| `actions/setup-java@v4` | Action cài Java |
| `run` | Chạy lệnh shell trực tiếp |

---

## 5. Job 2 — TEST (Parallel)

### 5.1 `needs: build` — Thứ Tự Chạy

```yaml
test-login:
  needs: build    # Chỉ chạy SAU KHI job "build" thành công
```

Nếu không có `needs`:
```
build ──┐
        ├── tất cả chạy cùng lúc ngay từ đầu
test ───┘   (sai! test chạy trước build)
```

Với `needs: build`:
```
build ──→ (xong) ──→ test-login       ┐
                  ──→ test-reg-faker  ├── 3 jobs này chạy song song
                  ──→ test-reg-millis ┘
```

### 5.2 Services — Tự Động Khởi Động Container

```yaml
services:
  nop_db:
    image: mcr.microsoft.com/mssql/server:2022-latest
    env:
      SA_PASSWORD: "Test@123456!"
      ACCEPT_EULA: "Y"
    ports:
      - 1433:1433    # Truy cập qua localhost:1433
    options: >-
      --health-cmd "..."   # Healthcheck: đợi SQL Server sẵn sàng

  nop_web:
    image: nopcommerceteam/nopcommerce:latest
    ports:
      - 8080:80      # Truy cập qua localhost:8080

  selenium:
    image: selenium/standalone-chrome:latest
    ports:
      - 4444:4444    # Truy cập qua localhost:4444
    options: >-
      --shm-size=2g  # Tăng shared memory cho Chrome
```

### 5.3 Chạy Maven Test

```yaml
- name: Run LoginTest
  env:
    BASE_URL: "http://localhost:8080"           # nopCommerce
    SELENIUM_HUB_URL: "http://localhost:4444/wd/hub"  # Selenium Grid
    DB_PASSWORD: ${{ secrets.DB_PASSWORD }}    # Lấy từ GitHub Secrets
  run: |
    mvn test \
      -Dsurefire.suiteXmlFiles=testng-login.xml \
      -DbaseUrl="${BASE_URL}" \
      -DgridUrl="${SELENIUM_HUB_URL}" \
      -Dheadless=true \
      -q
```

### 5.4 Upload Artifact — Lưu Kết Quả Test

```yaml
- name: Upload Allure results
  if: always()    # Chạy dù test PASS hay FAIL
  uses: actions/upload-artifact@v4
  with:
    name: allure-results-login    # Tên định danh (mỗi job dùng tên khác nhau)
    path: target/allure-results/
    retention-days: 1             # Xóa sau 1 ngày
```

---

## 6. Job 3 — REPORT


allure-report:
  needs: [ test-login, test-register-faker, test-register-millis ]
  if: always()    # Luôn chạy để có report dù test fail

  steps:
    # Tải kết quả từ 3 jobs về
    - uses: actions/download-artifact@v4
      with:
        name: allure-results-login
        path: allure-results/
      continue-on-error: true   # Không fail nếu artifact không tồn tại

    # Tạo report HTML
    - name: Generate Allure Report
      run: |
        wget allure-2.27.0.tgz && tar -xzf ...
        allure generate allure-results -o gh-pages --clean

    # Publish lên GitHub Pages
    - uses: actions/configure-pages@v4
    - uses: actions/upload-pages-artifact@v3
      with:
        path: gh-pages/
    - uses: actions/deploy-pages@v4
```

**Kết quả:** Report xuất hiện tại:
```
https://YOUR_USERNAME.github.io/nopcommerce-tests/


### Cú Pháp — KHÁC NHAU!

| | GitLab CI/CD | GitHub Actions |
|-|-------------|---------------|
| **Khai báo bước** | `script:` | `steps:` |
| **Chỉ định máy** | `image: maven:...` | `runs-on: ubuntu-latest` |
| **Action có sẵn** | YAML Anchors (`&anchor`) | `uses: actions/...@v4` |
| **Biến môi trường** | `variables:` global | `env:` per-step |
| **Secrets** | GitLab → Settings → CI/CD | GitHub → Settings → Secrets |
| **Report publish** | `pages:` job đặc biệt | `actions/deploy-pages` |

### Code Java — GIỐNG NHAU! ✅

```java
// BaseTest.java — hoạt động với cả GitLab lẫn GitHub Actions
String baseUrl = System.getProperty("baseUrl",
    System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));
```

---

## 8. GitHub Secrets

**Không bao giờ để password trong code!** Dùng GitHub Secrets.

### Thêm Secret vào GitHub

```
GitHub Repository → Settings → Secrets and variables → Actions
→ New repository secret
  Name:  DB_PASSWORD
  Value: Test@123456!
→ Add secret
```

### Dùng trong workflow

```yaml
env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}   # Cú pháp lấy secret
```

> **Masked tự động:** Secret bị ẩn khỏi log, không ai xem được dù có quyền admin.

---

## 9. Parallel Testing

### Song Song ở Cấp Độ Job (GitHub Actions)

```
needs: build xong → 3 jobs chạy cùng lúc:

  test-login          ─┐
  test-register-faker  ├── Chạy SONG SONG
  test-register-millis ─┘

Tuần tự: 2' + 2' + 2' = 6 phút
Parallel: max(2', 2', 2') = 2 phút ✅
```

### Song Song ở Cấp Độ Method (TestNG)

```xml
<suite name="Login Suite" parallel="methods" thread-count="3">
```

```
DataProvider có 5 rows:
  Tuần tự:   row1 → row2 → row3 → row4 → row5  = 25 giây
  Parallel:  row1+row2+row3 cùng lúc → row4+row5 cùng lúc = 10 giây ✅
```

> **Yêu cầu:** Dùng `ThreadLocal<WebDriver>` — project đã có sẵn ✅

---

## 10. Thiết Lập GitHub Trước Khi Push

### Bước 1 — Tạo repository trên GitHub

```
https://github.com → New repository
  Name: nopcommerce-tests
  Visibility: Public
  → Create repository
```

### Bước 2 — Enable GitHub Pages

```
GitHub Repository → Settings → Pages
  Source: GitHub Actions  (không phải branch!)
  → Save
```

### Bước 3 — Thêm Secret

```
GitHub Repository → Settings → Secrets and variables → Actions
  → New repository secret
  Name:  DB_PASSWORD
  Value: Test@123456!
  → Add secret
```

### Bước 4 — Push code

```bash
cd /path/to/nopcommerce-local

# Xóa remote GitLab cũ (nếu có)
git remote remove origin

# Thêm remote GitHub mới
git remote add origin https://github.com/YOUR_USERNAME/nopcommerce-tests.git

git add .
git commit -m "ci: migrate to GitHub Actions with parallel testing"
git push -u origin main
```

### Bước 5 — Xem workflow chạy

```
GitHub Repository → Actions tab → Click vào workflow mới nhất
```

---

## 11. Cheat Sheet — Lệnh Nhanh

| Tình huống | Lệnh |
|-----------|------|
| Chạy test local bình thường | `mvn test` |
| Chạy test headless local | `mvn test -Dheadless=true` |
| Chạy chỉ 1 suite | `mvn test -Dsurefire.suiteXmlFiles=testng-login.xml` |
| Trigger workflow thủ công | GitHub → Actions → Run workflow |
| Xem log chi tiết | GitHub → Actions → Click job → Expand step |
| Xem Allure Report online | `https://YOUR_USERNAME.github.io/nopcommerce-tests/` |
| Download test artifacts | Actions → workflow run → Artifacts section |

---

## 📁 Các File Liên Quan

| File | Vị trí | Mô tả |
|------|--------|-------|
| `nopcommerce-tests.yml` | `.github/workflows/` | Workflow chính |
| `testng-login.xml` | Root project | Suite chạy LoginTest (parallel methods) |
| `testng-register-faker.xml` | Root project | Suite chạy RegisterTest_JavaFaker |
| `testng-register-millis.xml` | Root project | Suite chạy RegisterTest_CurrentTimeMillis |
| `BaseTest.java` | `src/test/java/...` | Hỗ trợ RemoteWebDriver + đọc env var |
| `LoginTest.java` | `src/test/java/...` | Đọc baseUrl từ env var |
