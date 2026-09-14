# Лаборатори №2 — Гүйцэтгэлийн хэмжүүрийг k6-аар хэмжих

**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

**Оюутны нэр:** Ulemj Ganbat

**Оюутны код:** B232270805

## Бай ба орчин

Бүх load тестийг зааварт зөвшөөрсөн `https://test.k6.io` дадлагын сайт руу хийсэн. Ажиллуулахын өмнө `script.js`-ийн `TARGET_URL` болон бусад `.js` файлыг шалгаж, өөр бай байхгүйг баталгаажуулсан. Сургууль болон бусад бодит сайт руу тест хийгээгүй. Хэмжилт 2026-09-14-нд Windows/amd64 орчинд k6 v2.1.0-аар хийгдсэн; сүлжээ ба машин өөрчлөгдвөл тоо өөр гарч болно.

`k6 version` командын бодит гаралт:

```text
k6.exe v2.1.0 (commit/83a87a41e2, go1.26.4, windows/amd64)
```

## Анхны baseline ба SLO

Алхам 2-ын `script.js`-ийг 5 VU, 30 секунд ажиллуулахад `http_req_duration`-ийн avg **138.59 ms**, p90 **224.98 ms**, p95 **225.93 ms** гарсан. Нийт **230** HTTP хүсэлт буюу **7.537439 хүсэлт/с**, `http_req_failed` **0.00% (0/230)** байв. Эх сурвалж: [baseline-ийн бүтэн гаралт](results/baseline-05vu-30s.txt).

Өөрийн baseline p95 болох **225.93 ms**-ийг **1.5**-аар үржүүлэхэд **338.895 ms** тул latency SLO-г `http_req_duration: ['p(95)<339']` гэж урьдчилан тогтоосон. 50% нэмэлт нөөц нь ердийн сүлжээний хэлбэлзлийг даах боловч baseline-ээс мэдэгдэхүйц удааширсан үед quality gate-ийг унагахаар сонгосон хэмжээ юм. Алдааны SLO-г `http_req_failed: ['rate<0.01']`, өөрөөр хэлбэл **1%-иас бага**, гэж тогтоосон. Энэ утгуудыг харьцуулалтын үр дүнг харахаас **өмнө** `thresholds.js`-д бичсэн бөгөөд PASS болгохын тулд дараа нь сулруулаагүй.

30 VU, 1 минутын [SLO туршилт](results/threshold-pass.txt) **PASS** гарсан: p95 **227.09 ms < 339 ms**, алдаа **0.00% < 1%**. Quality gate-ийг шалгахын тулд тусдаа `thresholds-fail.js`-д latency хязгаарыг зориуд **50 ms** болгож, [FAIL гаралт](results/threshold-fail.txt)-д p95 **226.35 ms** байснаар latency threshold **FAIL**, харин error-rate threshold **PASS** болсныг харуулсан. `thresholds-fail.js` нь бодит SLO-г орлохгүй.

## 5, 30, 100 VU-ийн тусдаа хэмжилт

Түвшин бүрийг `script.js`-ээр **тус тусад нь 1 минут** ажиллуулсан. Дараах утгууд нэгтгэсэн stages summary-гаас бус, мөр бүрийн холбоостой бүтэн text гаралтын `http_req_duration`, `http_reqs`, `http_req_failed` хэсгээс авсан.

| VU | `http_req_duration` avg | p90 | p95 | `http_reqs` нийт | Throughput (`http_reqs`/с) | Error rate | Бүтэн гаралт | Summary зураг |
|---:|---:|---:|---:|---:|---:|---:|---|---|
| 5 | 140.52 ms | 225.08 ms | 226.4 ms | 468 | 7.633595 | 0.00% (0/468) | [run-05vu.txt](results/run-05vu.txt) | [PNG](screenshots/run-05vu.png) |
| 30 | 140.32 ms | 225.66 ms | 226.38 ms | 2806 | 45.842173 | 0.00% (0/2806) | [run-30vu.txt](results/run-30vu.txt) | [PNG](screenshots/run-30vu.png) |
| 100 | 143 ms | 225.81 ms | 226.38 ms | 9240 | 150.712593 | 0.00% (0/9240) | [run-100vu.txt](results/run-100vu.txt) | [PNG](screenshots/run-100vu.png) |

100 VU-д p95 өсөөгүй ч хамгийн удаан хүсэлт **771.79 ms** болсон (5 VU-д **278.26 ms**, 30 VU-д **359.54 ms**). Иймээс энэ туршилтаар нийтлэг хэрэглэгчийн p95 туршлага доройтсон цэг ажиглагдаагүй, харин **100 VU-д ховор удаан хүсэлтийн эрсдэл** илүү тодорсон гэж дүгнэв.

## Stages туршилт

`stages.js` нь 30 секундэд 5 VU хүргэж, 1 минутын турш 30 VU хүртэл, дараагийн 30 секундэд 100 VU хүртэл өсгөн, сүүлийн 30 секундэд 0 VU болгон бууруулсан. [Бүтэн stages гаралт](results/stages.txt)-д 100 max VU, **7,122** HTTP хүсэлт, **47.223699 хүсэлт/с**, p95 **226.34 ms**, **0.00%** HTTP failure гарсан. Энэ нь 2 минут 30 секундын **нэгтгэсэн** summary тул дээрх 5/30/100 VU-ийн хүснэгтэд ашиглаагүй.

## Дүгнэлт

5 VU-ийн нэг минутын туршилтаар throughput 7.633595 хүсэлт/с, p95 226.4 ms байв. 30 VU-д throughput 45.842173 хүсэлт/с болж ойролцоогоор зургаа дахин өссөн. 100 VU-д throughput 150.712593 хүсэлт/с хүрч, ачаалал өсөхөд бүтээмж хэрхэн өөрчлөгдөхийг харууллаа. p90 нь 225.08-аас 225.81 ms хүртэл маш бага өөрчлөгдсөн. p95 нь 5 VU-д 226.4 ms, 30 ба 100 VU-д 226.38 ms байсан тул энэ орчинд түгээмэл хэрэглэгчийн latency доройтол ажиглагдсангүй. Харин хамгийн удаан хүсэлт 100 VU-д 771.79 ms хүрсэн нь тусдаа хүсэлтийн tail latency муудаж болдгийг харуулсан. Гурван түвшинд HTTP алдаа 0.00% байсан нь тухайн тестийн хугацаанд найдвартай ажиллагаа хэвээр байсныг харуулсан. Baseline p95 225.93 ms-ээс гаргасан 339 ms SLO-г 30 VU-ийн p95 227.09 ms хангаж PASS болсон. Зориудын 50 ms threshold нь p95 226.35 ms үед FAIL болж, CI quality gate хэтэрсэн үзүүлэлтийг автоматаар барих зарчмыг үзүүлсэн.

## Гаралт ба дахин ажиллуулах

Репозиторт [baseline](results/baseline-05vu-30s.txt), [5 VU](results/run-05vu.txt), [30 VU](results/run-30vu.txt), [100 VU](results/run-100vu.txt), [stages](results/stages.txt), [PASS](results/threshold-pass.txt), [FAIL](results/threshold-fail.txt) **бүтэн текст гаралтууд** байгаа. [5 VU](screenshots/run-05vu.png), [30 VU](screenshots/run-30vu.png), [100 VU](screenshots/run-100vu.png), [PASS](screenshots/threshold-pass.png), [FAIL](screenshots/threshold-fail.png) summary зургуудыг эдгээр бодит текст гаралтаас [render_summary_images.py](render_summary_images.py)-аар үүсгэсэн; зураг нь гаралтын файлыг орлохгүй. Windows PowerShell 5.1-ийн `Tee-Object` анх UTF-16 текст бичсэн тул репозиторт хуулахдаа хэмжүүрийг өөрчлөлгүй UTF-8 болгосон.

Өөрийн орчинд `k6`-г [Grafana-ийн заавраар](https://grafana.com/docs/k6/latest/set-up/install-k6/) суулгаад, **бай URL-г нягталсны дараа** репозиторийн үндэс хавтсаас дараах командуудыг ажиллуулна. PowerShell дээр `| Tee-Object -FilePath ...` нэмж гаралтыг файлд хадгалж болно.

```powershell
k6 version
k6 run script.js
k6 run --vus 5 --duration 1m script.js
k6 run --vus 30 --duration 1m script.js
k6 run --vus 100 --duration 1m script.js
k6 run stages.js
k6 run thresholds.js
k6 run thresholds-fail.js
```

Сүүлийн команд зориудын FAIL тул процессын гарах код 0 биш байна.
