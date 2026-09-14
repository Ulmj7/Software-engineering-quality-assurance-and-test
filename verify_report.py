"""Check that the README table matches the saved k6 console summaries."""

from pathlib import Path
import re


ROOT = Path(__file__).resolve().parent
README = (ROOT / "README.md").read_text(encoding="utf-8")


def summary(path: Path) -> tuple[str, str, str, str, str, str]:
    output = path.read_text(encoding="utf-8")
    duration = re.search(
        r"^\s+http_req_duration\.+:\s+avg=(\S+).*?p\(90\)=(\S+)\s+p\(95\)=(\S+)",
        output,
        re.MULTILINE,
    )
    requests = re.search(r"^\s+http_reqs\.+:\s+(\d+)\s+([\d.]+)/s", output, re.MULTILINE)
    failures = re.search(
        r"^\s+http_req_failed\.+:\s+([\d.]+%)\s+(\d+) out of (\d+)",
        output,
        re.MULTILINE,
    )
    assert duration and requests and failures, f"Missing k6 metrics in {path}"
    assert failures.group(3) == requests.group(1), f"Request counts disagree in {path}"
    return (
        duration.group(1),
        duration.group(2),
        duration.group(3),
        requests.group(1),
        requests.group(2),
        f"{failures.group(1)} ({failures.group(2)}/{failures.group(3)})",
    )


for vus in (5, 30, 100):
    path = ROOT / "results" / f"run-{vus:02d}vu.txt" if vus < 100 else ROOT / "results" / "run-100vu.txt"
    avg, p90, p95, requests, throughput, error_rate = summary(path)
    row = re.search(rf"^\| {vus} \|.*$", README, re.MULTILINE)
    assert row, f"Missing README table row for {vus} VU"
    cells = [cell.strip() for cell in row.group().strip("|").split("|")]
    expected = [str(vus), avg, p90, p95, requests, throughput, error_rate]
    assert [x.replace(" ", "") for x in cells[:7]] == [x.replace(" ", "") for x in expected], (
        f"{vus} VU table mismatch: {cells[:7]} != {expected}"
    )
    assert (ROOT / "screenshots" / f"run-{vus:02d}vu.png").is_file() if vus < 100 else (ROOT / "screenshots" / "run-100vu.png").is_file()

baseline = (ROOT / "results" / "baseline-05vu-30s.txt").read_text(encoding="utf-8")
baseline_p95 = re.search(r"http_req_duration\.+:.*p\(95\)=([\d.]+)ms", baseline)
assert baseline_p95, "Baseline p95 missing"
limit = round(float(baseline_p95.group(1)) * 1.5)
assert f"p(95)<{limit}" in (ROOT / "thresholds.js").read_text(encoding="utf-8")
assert "✓ 'p(95)<339'" in (ROOT / "results" / "threshold-pass.txt").read_text(encoding="utf-8")
assert "✗ 'p(95)<50'" in (ROOT / "results" / "threshold-fail.txt").read_text(encoding="utf-8")

scripts = list(ROOT.glob("*.js"))
urls = [url for script in scripts for url in re.findall(r"https?://[^\s'\"]+", script.read_text(encoding="utf-8"))]
assert urls == ["https://test.k6.io"], f"Unexpected load-test target URLs: {urls}"

print("README table, baseline SLO, threshold outcomes, evidence images, and target URL match.")
