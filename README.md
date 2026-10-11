# Лаборатори №5: API систем тест — Postman ба Newman

**Оюутны нэр:** Г. Үлэмж  
**Оюутны код:** B232270805  
**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

## Орчин

Windows PowerShell дээр локал API болон Newman CLI ашиглана. Багшийн өгсөн `server.js`-ийг өөрчлөхгүй.

`node -v`:

```text
v24.16.0
```

`newman -v`:

```text
6.2.3
```

Newman нь `package-lock.json`-д хувилбар нь тогтоогдсон локал dependency. `npm ci`-ийн дараа PowerShell-д `.\node_modules\.bin\newman.cmd` командаар ажиллуулна.

## Даалгавар 1 — Анхны хүсэлт ба oracle

`lab05-collection.json` нь Postman Collection v2.1 форматтай. Collection variable `baseUrl` нь `http://localhost:3000`.

S01 өөрийн setup-аар `PUT /students/B231234567` хүсэлтэд `{"status":"active","coursesTaken":["CS201"]}`, `PUT /courses/CS313` хүсэлтэд `{"prerequisites":["CS201"]}` өгнө. Дараа нь `POST /registrations` хүсэлтэд `{"studentID":"B231234567","courseID":"CS313"}` илгээнэ. POST дээр хоёр `pm.test` oracle нь HTTP `201` болон `result="OK"`-г тус тус шалгана. Хоёр дахь oracle нь `registrationID` тоо эсэхийг мөн шалгах бөгөөд яг утгыг нь тогтоохгүй.

## Даалгавар 2 — Тест дизайны таван алхам

### 1. Бие даан тестлэгдэх функц

`POST /registrations` нь оюутны төлөв, хичээлийн оршин байдал болон урьдач нөхцөлийг шалгаж бүртгэл үүсгэнэ. `PUT /students/:id`, `PUT /courses/:id` нь тест бүрийн initialization буюу setup; үндсэн функцийн HTTP статус ба JSON `result` нь oracle болно.

### 2–3. Сонголт ба төлөөлөх утга

| Сонголт | Эквивалент анги / утга | Төлөөлөх бодит утга | Хамаарал |
| --- | --- | --- | --- |
| studentID-гийн хүчинтэй байдал | Байгаа, идэвхтэй | `S04_ACTIVE`, `status="active"` | Өөрийн PUT setup-аар үүсгэнэ |
| studentID-гийн хүчинтэй байдал | Байгаа, идэвхгүй | `S03_INACTIVE`, `status="inactive"` | `status != "active"` ангийг төлөөлнө |
| studentID-гийн хүчинтэй байдал | Байхгүй | `S02_MISSING_<UUID>` | Шинэ UUID үүсгээд тухайн ID-д PUT хийхгүй |
| Оюутны үзсэн хичээлүүд | Урьдач нөхцөл хангана | `coursesTaken=["CS201"]`, `prerequisites=["CS201"]` | Байгаа оюутан ба байгаа хичээл дээр үнэлнэ |
| Оюутны үзсэн хичээлүүд | Урьдач нөхцөл хангахгүй | `coursesTaken=["CS101"]`, `prerequisites=["CS201","CS202"]` | Бүх урьдачийг үзээгүй |
| Оюутны үзсэн хичээлүүд | Урьдачийн заримыг хангана | `coursesTaken=["CS201"]`, `prerequisites=["CS201","CS202"]` | Дутуу урьдач нь зөвхөн `CS202` |
| Оюутны үзсэн хичээлүүд | Хоосон массив | `coursesTaken=[]` | Урьдачгүй хичээлд хангана; урьдачтай хичээлд хангахгүй |
| courseID-гийн хүчинтэй байдал | Байгаа | `C03_TARGET` | Өөрийн PUT setup-аар үүсгэнэ |
| courseID-гийн хүчинтэй байдал | Байхгүй | `S04_MISSING_<UUID>` | Шинэ UUID үүсгээд тухайн ID-д PUT хийхгүй |
| Хичээлийн урьдач нөхцөл | Бүгдийг үзсэн | `["CS201"]`, оюутан `["CS201"]` | Урьдачийн олонлог нь үзсэн хичээлийн дэд олонлог |
| Хичээлийн урьдач нөхцөл | Огт үзээгүй | `["CS201","CS202"]`, оюутан `["CS101"]` | Дутуу жагсаалт `["CS201","CS202"]` |
| Хичээлийн урьдач нөхцөл | Заримыг үзсэн | `["CS201","CS202"]`, оюутан `["CS201"]` | Дутуу жагсаалт `["CS202"]` |
| Хичээлийн урьдач нөхцөл | Урьдачгүй | `prerequisites=[]` | Хоосон `coursesTaken` ч хангана |
| Хүсэлтийн формат | studentID талбар дутуу | `{"courseID":"C11_TARGET"}` | HTTP 400 |
| Хүсэлтийн формат | courseID талбар дутуу | `{"studentID":"S12_ACTIVE"}` | HTTP 400 |
| Хүсэлтийн формат | Буруу JSON | Төгсгөлийн `}` тэмдэггүй JSON | HTTP 400 |

Оюутан байхгүй байхад түүний `coursesTaken`-г, хичээл байхгүй байхад түүний `prerequisites`-г бодит өгөгдөлтэй гэж хослуулах боломжгүй. Тиймээс эдгээр нь тухайн мөрөнд N/A. «Бүх урьдачийг үзсэн» ба «урьдач хангахгүй» нь нэг ижил байгаа оюутан/хичээлийн хосын хувьд зөрчилтэй сонголтууд. «Заримыг үзсэн» ангид хоёр ба түүнээс олон урьдач шаардлагатай; ганц урьдачтай хичээл дээр энэ ангийг үүсгэх боломжгүй.

### 4. Спецификаци

Доорх нь хүлээгдэх үр дүн. S06–S07 нь давхар алдааны үед ямар шалгалт түрүүлэхийг туршилтаар тогтооно.

| ID | Стратеги / нөхцөл | Оюутны setup | Хичээлийн setup / POST онцлог | Хүлээгдэх HTTP | Хүлээгдэх result / нэмэлт утга |
| --- | --- | --- | --- | --- | --- |
| S01 | Happy path | `B231234567`: active, `[CS201]` | `CS313`: `[CS201]` | 201 | `OK`, registrationID нь тоо |
| S02 | Оюутан байхгүй | Шинэ UUID; тэр ID-д PUT хийхгүй | `C02_TARGET`: `[CS201]` | 200 | `ERROR_NO_STUDENT` |
| S03 | Оюутан идэвхгүй | `S03_INACTIVE`: inactive, `[CS201]` | `C03_TARGET`: `[CS201]` | 200 | `ERROR_INACTIVE_STUDENT` |
| S04 | Хичээл байхгүй | `S04_ACTIVE`: active, `[CS201]` | Шинэ UUID; тэр ID-д PUT хийхгүй | 200 | `ERROR_NO_COURSE` |
| S05 | Урьдачийг огт үзээгүй | `S05_ACTIVE`: active, `[CS101]` | `C05_TARGET`: `[CS201,CS202]` | 200 | `ERROR_PREREQUISITES`, missing=`[CS201,CS202]` |
| S06 | Давхар: оюутан ба хичээл байхгүй | Хяналтын `S06_CONTROL`-г үүсгэнэ; POST-д өөр шинэ UUID | Хяналтын `C06_CONTROL`-г үүсгэнэ; POST-д өөр шинэ UUID | 200 | `ERROR_NO_STUDENT` |
| S07 | Давхар: идэвхгүй оюутан, хичээл байхгүй | `S07_INACTIVE`: inactive, `[]` | Шинэ UUID; тэр ID-д PUT хийхгүй | 200 | `ERROR_INACTIVE_STUDENT` |
| S08 | Хязгаар: урьдачгүй, үзсэн хичээл хоосон | `S08_ACTIVE`: active, `[]` | `C08_TARGET`: `[]` | 201 | `OK`, registrationID нь тоо |
| S09 | Хязгаар: хоосон coursesTaken, урьдачтай | `S09_ACTIVE`: active, `[]` | `C09_TARGET`: `[CS201]` | 200 | `ERROR_PREREQUISITES`, missing=`[CS201]` |
| S10 | Урьдачийн заримыг үзсэн | `S10_ACTIVE`: active, `[CS201]` | `C10_TARGET`: `[CS201,CS202]` | 200 | `ERROR_PREREQUISITES`, missing=`[CS202]` |
| S11 | Хязгаар: studentID талбар дутуу | `S11_ACTIVE`: active, `[CS201]` | `C11_TARGET`: `[CS201]`; POST=`{"courseID":"C11_TARGET"}` | 400 | `ERROR_BAD_REQUEST` |
| S12 | Хязгаар: courseID талбар дутуу | `S12_ACTIVE`: active, `[CS201]` | `C12_TARGET`: `[CS201]`; POST=`{"studentID":"S12_ACTIVE"}` | 400 | `ERROR_BAD_REQUEST` |
| S13 | Хязгаар: JSON буруу | `S13_ACTIVE`: active, `[CS201]` | `C13_TARGET`: `[CS201]`; POST JSON төгсгөлийн `}`-гүй | 400 | `ERROR_BAD_JSON` |

### 5. Бодит тест кейс

Спецификаци бүр collection-ийн тусдаа S01–S13 хавтас болно. Тухайн хавтас өөрт хэрэгтэй бичлэгээ PUT-ээр тохируулж, POST хүсэлтээ илгээн, статус ба result-ийг өөр өөр `pm.test`-ээр шалгана. Урьдач дутуу гурван кейс `missing` массивыг тусдаа oracle-аар шалгана. Байхгүй бичлэгийг PUT-ээр үүсгэж болохгүй тул тэдгээрийн ID-г POST-ийн pre-request script шинэ UUID-аар үүсгэнэ; S06-д setup нь хяналтын хоёр бичлэгт хийгдэнэ.

## Даалгавар 3 — Collection ба бие даасан байдал

`lab05-collection.json` файлыг Postman-ийн Import хэсгээр оруулж ажиллуулж болно. Хавтас бүр өөрийн setup-аа агуулна; бусад хавтасны өгөгдөлд найдахгүй. PUT нь өмнөх бичлэгийг бүхэлд нь дахин бичнэ, харин байхгүй оюутан/хичээлийн ID нь ажиллуулах бүрд шинэ UUID байна. `registrationID` серверийн хугацаанд хуримтлагдах тул амжилтын oracle зөвхөн тоон төрөл эсэхийг шалгана. Collection-ийг серверийг дахин асаалгүй ажиллуулсны дараа хавтас бүрийг урвуу дарааллаар дангаар ажиллуулж, бүгд амжилттай болсноор бие даасан байдлыг шалгасан.

## Даалгавар 4 — Newman-ийн бүтэн текст нотолгоо

Үндсэн collection-ийн **тест/assertion-ы нийт тоо 75** бөгөөд энэ нь [results/newman-pass.txt](results/newman-pass.txt)-ийн `assertions` мөрийн `executed` тоотой яг таарна. Спецификацийн 13 сценарийг setup-тай нь ажиллуулахад 36 HTTP хүсэлт илгээнэ. PUT setup бүрийн статус ба result oracle мөн Newman-ийн assertions тоонд ордог.

| Ажиллуулалт / бүтэн гаралт | Iterations executed / failed | Requests executed / failed | Assertions executed / failed | Newman exit code |
| --- | --- | --- | --- | --- |
| [PASS](results/newman-pass.txt) | 1 / 0 | 36 / 0 | 75 / 0 | 0 |
| [FAIL](results/newman-fail.txt) | 1 / 0 | 36 / 0 | 75 / 1 | 1 |
| [DOWN](results/newman-down.txt) | 1 / 0 | 36 / 36 | 75 / 75 | 1 |

FAIL-д `lab05-collection-fail.json` гэсэн тусдаа collection ашигласан: зөвхөн S01 POST-ийн статусын oracle-д бодит `201`-ийн оронд `200` хүлээлгэсэн. `result="OK"` oracle болон үндсэн `lab05-collection.json` зөв хэвээр. Гаралт дахь `expected response to have status code 200 but got 201` нь зориудын oracle алдаа, харин exit code 1 нь CI quality gate тухайн ажиллуулалтыг зогсоох нөхцөл болно.

DOWN-д API серверийг Ctrl+C-ээр унтраасны дараа үндсэн collection-ийг ажиллуулсан. `connect ECONNREFUSED 127.0.0.1:3000` нь сервертэй холбогдож чадаагүй интерфейсийн алдаа бөгөөд FAIL-ийн HTTP хариуг буруу хүлээсэн oracle алдаанаас ялгаатай. DOWN үед HTTP хариу байхгүйгээс статус, JSON oracle-ууд давхар унадаг; үндсэн шалтгаан нь холболтгүй байдал.

Бүх гаралт бүтнээрээ UTF-8 текстээр хадгалагдсан; файлын төгсгөлийн `exit=...` нь ажиллуулалтын дараа `$LASTEXITCODE`-оос авсан Newman-ийн exit code. Node 24 дээр Newman-ы dependency-ээс гарсан `DEP0176` анхааруулга ч бүтэн гаралтад хадгалагдсан.

### Давтан ажиллуулах (PowerShell)

Эхний терминал:

```powershell
npm.cmd ci
node server.js
```

Өөр терминалд дараах функцийг тодорхойлно. `cmd.exe` нь stderr-ийг stdout-д нэгтгэнэ; `$LASTEXITCODE`-г pipeline-ийн дараа шууд авна. `Tee-Object`-ийн файлыг дараа нь UTF-8 болгон хадгална.

```powershell
New-Item -ItemType Directory -Force -Path results | Out-Null
function Save-NewmanRun($collection, $report) {
    cmd.exe /d /c ".\node_modules\.bin\newman.cmd run $collection --color off 2>&1" |
        Tee-Object -FilePath $report
    $newmanExit = $LASTEXITCODE
    $fullOutput = Get-Content -LiteralPath $report -Raw
    [System.IO.File]::WriteAllText(
        (Join-Path (Get-Location) $report),
        $fullOutput + "`nexit=$newmanExit`n",
        [System.Text.UTF8Encoding]::new($false)
    )
    Write-Output "Newman exit=$newmanExit"
}
Save-NewmanRun lab05-collection.json results/newman-pass.txt
Save-NewmanRun lab05-collection-fail.json results/newman-fail.txt
```

Эхний терминал дахь серверийг **Ctrl+C**-ээр унтраагаад:

```powershell
Save-NewmanRun lab05-collection.json results/newman-down.txt
```

Серверийг дараа нь `node server.js`-ээр дахин асааж болно. Нотолгоог шинэчилбэл дээрх хүснэгтийн тоонууд, exit code-ийг шинэ гаралттай тулгаж шинэчилнэ.

## Даалгавар 5 — Нийтийн API (нэмэлт гүнзгийрэл)

[lab05-public-collection.json](lab05-public-collection.json) нь [JSONPlaceholder /users](https://jsonplaceholder.typicode.com/users) endpoint-д GET хүсэлт илгээж, HTTP 200, хариу массив эсэх, эхний хэрэглэгчийн нэр `Leanne Graham` эсэх гэсэн гурван тусдаа `pm.test` oracle ажиллуулна. [results/newman-public.txt](results/newman-public.txt)-д iterations 1 / 0, requests 1 / 0, assertions 3 / 0, exit code 0 гэсэн бүтэн гаралт бий. Энэ нэмэлт collection нь үндсэн collection-ийн 75 assertion-д орохгүй.

GET-only нийтийн API нь PUT setup болон төлөвийн удирдлага шаардахгүй тул локал бүртгэлийн API-гаас хялбар боловч интернет холболт, гаднын үйлчилгээний бэлэн байдал болон өгөгдлийн өөрчлөлтөөс хамаарах нь илүү хэцүү байв.

```powershell
Save-NewmanRun lab05-public-collection.json results/newman-public.txt
```

## GitHub ба илгээх журам

Лабын public репозиторийн холбоос: [Software-engineering-quality-assurance-and-test — lab5](https://github.com/Ulmj7/Software-engineering-quality-assurance-and-test/tree/lab5).

Даалгавар 1–5-ын үе шат бүр дуусахад тусдаа commit хийсэн. Багшийн `.docx`, `node_modules/`, `.idea/`, `.DS_Store`, түр хэрэгслүүдийг `.gitignore`-оор хассан. Репод `server.js`, үндсэн болон FAIL collection, нэмэлт public collection, `results/` бүтэн текстүүд, `README.md`, давтагдах dependency суулгалтын `package.json` ба `package-lock.json` орно.

MS Teams-ийн тухайн Assignment-д дээрх **lab5 салбарын линкийг** хугацаанаас өмнө хавсаргаж, **Turn in** товчийг заавал дарна; линк хавсаргасан төдийд илгээгдсэнд тооцогдохгүй. Teams-д илгээсэн эсэх нь GitHub-д байршуулснаас тусдаа үйлдэл.

## Дүгнэлт

Тест дизайны таван алхмаас сонголтуудыг эквивалент ангид хувааж, зөв хослолын спецификаци болгох үе хамгийн их бодол шаардсан. Оюутан байхгүй мөртөө түүний үзсэн хичээлүүдийг бодит өгөгдөлтэй гэж үзэх, эсвэл хичээл байхгүй мөртөө урьдачийг нь шалгах хослол боломжгүй байв. Ийм хамааралтай сонголтуудыг N/A гэж тэмдэглэснээр зөрчилтэй тест дизайн үүсгэхээс зайлсхийсэн. Давхар алдааны туршилтууд оюутан байхгүй алдаа хичээл байхгүй алдаанаас, идэвхгүй оюутны алдаа мөн хичээл байхгүй алдаанаас түрүүлдгийг баталсан. Урьдачгүй хичээлд хоосон coursesTaken зөвшөөрөгдөх боловч урьдачтай хичээлд алдаа болохыг хязгаарын кейсүүд харуулсан. Өөрийн PUT setup, шинэ UUID болон registrationID-ийн төрлийг шалгах oracle нь серверийн хуримтлагдсан төлөвөөс тестүүдийг хараат бус болгосон. Шалгасан спецификацийн хүрээнд API-гийн хариу хүлээлттэй таарсан тул бүтээгдэхүүний согог илрээгүй. Зориуд буруу oracle оруулахад Newman-ийн exit code алдааг илэрхийлж, автомат чанарын хаалт хийх боломжийг баталлаа. Сервер унтарсан үеийн ECONNREFUSED-ийг oracle-ийн алдаанаас ялгаж тайлбарлах нь доголдлын үндсэн шалтгааныг зөв тогтооход хэрэгтэй байв.
