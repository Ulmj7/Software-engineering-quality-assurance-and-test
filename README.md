# Лаборатори №6: Тест тохиолдол боловсруулах — Category-Partition ба PICT

**Оюутны нэр:** Г. Үлэмж  
**Оюутны код:** B232270805  
**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

Энэ лаб кодгүй, тест **дизайны** лаб. Гол бүтээгдэхүүн нь README-ийн хүснэгтүүд, PICT-ийн model файлууд, `results/` дахь текст гаралтууд юм.

## Орчин ба PICT-ийн бүтээлт

Заавар Ubuntu Linux-д зориулагдсан тул Windows 11 дээр **WSL2 (Ubuntu)** ашиглав. PICT-ийг бэлэн binary-аар биш **эх кодоос** өөрөө бүтээв. Эх код нь лабын репоос гадна `~/tools/pict` хавтсанд байгаа тул репод орохгүй. Нэмээд `pict/`, `build/`-ийг `.gitignore`-д оруулсан.

| Хэрэгсэл | Хувилбар |
| --- | --- |
| cmake | 4.2.3 |
| g++ | 15.2.0 |
| make | 4.4.1 |
| PICT эх код | `microsoft/pict`, commit `ab76c25` (2026-09-08) |

Ажиллуулсан командууд (зааврын дагуу):

```bash
sudo apt update && sudo apt install -y git cmake g++ make
mkdir -p ~/tools && cd ~/tools && git clone https://github.com/microsoft/pict && cd pict
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build -j4
sudo cp build/cli/pict /usr/local/bin/
pict   # хэрэглээний заавар хэвлэгдсэн, амжилттай
```

`cmake` нэг оролдлогоор амжилттай болсон тул `make` хувилбар хэрэглээгүй. Энэ WSL-д `sudo` нууц үг асуудаг тул `apt install` ба `cp` алхмыг `wsl -u root`-оор ажиллуулсан, бусад алхам энгийн хэрэглэгчээр ажилласан.

Нотолгоог лабын репо руу буцаж зааврын командаар текст файлд хадгалсан ([results/pict-build.txt](results/pict-build.txt)):

```bash
mkdir -p results
(git -C ~/tools/pict log -1 --format='pict commit %h %ad' --date=short; pict 2>&1 | head -3) | tee results/pict-build.txt
```

```text
pict commit ab76c25 2026-09-08
Pairwise Independent Combinatorial Testing

Usage: pict model [options]
```

## Хэсэг A: Category-Partition гараар — шилжүүлгийн функц

Тестлэх функц: `POST /transfer {"from": ID, "to": ID, "amount": NUM, "currency": CODE}`. Код байхгүй, зөвхөн тодорхойлолтоос тест дизайн хийнэ.

### Таамаглал ба хүрээ

Тодорхойлолтод ил бичигдээгүй зүйлийг доор тогтоов.

1. **Данс = `from` данс.** Үлдэгдэл (B), өдрийн лимит, валют нь `from` дансных. `currency` нь `from` дансны валюттай ижил (Same) эсвэл өөр (Different). Өөр бол `amount`-ийг `from` дансны валют руу хөрвүүлж, үлдэгдэл ба лимитийг **хөрвүүлсний дараах** дүн `a`-аар шалгана.
2. **Өдрийн лимит хуримтлагддаг.** Үлдсэн лимит R = лимит − өнөөдөр аль хэдийн шилжүүлсэн дүн. Тиймээс нэг удаагийн дүн лимитээс бага байсан ч R-ээс их байж болно.
3. **Тестийн орчин (fixture).** Ханш (stub) 1 USD = 3000 MNT. `ACC-100`: MNT, үлдэгдэл 1,000,000, лимит 500,000. `ACC-POOR`: MNT, үлдэгдэл 100,000, лимит 500,000. `ACC-FRZ`: царцаагдсан. `ACC-200`: хүлээн авагч. `ACC-404`, `ACC-405`: байхгүй.
4. **Хүрээнээс гаргасан зүйл.** ID-ийн формат буруу эсвэл талбар дутуу хүсэлт, мэдэгдэхгүй валютын код, царцаагдсан `to` данс, хөрвүүлэлтийн бөөрөнхийлөлт. Тодорхойлолтод эдгээрийн хүлээгдэх үр дүн байхгүй тул загварт оруулаагүй.
5. **Давуу эрэмбэ (олон алдаа зэрэг байвал).** `NO_FROM → FROZEN → NO_TO → SAME_ACCOUNT → BAD_AMOUNT → INSUFFICIENT → LIMIT`. Үндэслэл: үлдэгдэл, лимитийг харьцуулахын тулд `from` данс эхлээд олдож царцаагүй байх ёстой тул шалгалтыг хүсэлтийн талбарын дарааллаар (from → to → amount) хийж, дүнгийн дотор тодорхойлолтын жагсаалтын дарааллыг (дүн ≤ 0 → үлдэгдэл → лимит) баримтлав. Дүн ≤ 0-ийг дансаас үл хамааран түрүүлж шалгадаг хэрэгжилт ч бий, тодорхойлолт заагаагүй тул энэ нь тест дизайны шийдвэр юм.

### A1: Сонголт ба төлөөлөх утга

`a` нь хөрвүүлсний дараах дүн, `B` үлдэгдэл, `R` үлдсэн лимит.

| Категори | Сонголт | Төлөөлөх бодит утга | Тайлбар |
| --- | --- | --- | --- |
| **From** | Valid | `ACC-100` | байгаа, царцаагүй |
| | Frozen | `ACC-FRZ` | байгаа, царцаагдсан |
| | Missing | `ACC-404` | ийм данс байхгүй |
| **To** | Valid | `ACC-200` | байгаа, `from`-оос өөр |
| | Missing | `ACC-405` | ийм данс байхгүй |
| | SameAsFrom | `to` = `from` = `ACC-100` | `from`-той ижил ID |
| **Amount** | Normal | 150,000 MNT (= `50 USD`) | 0 < a ≤ min(B, R) |
| | Zero | 0 | a = 0 |
| | BelowZero | −1,000 | a < 0 |
| | OverBalance | `ACC-POOR`-д 150,000 MNT | a > B, гэхдээ a ≤ R |
| | OverLimit | Empty: 600,000 MNT (= `200 USD`); Partly: 300,000 MNT (= `100 USD`) | a ≤ B, гэхдээ a > R |
| | ExactBalance | `ACC-POOR`-д 100,000 MNT | a = B |
| **Currency** | Same | `MNT` | `from` дансны валюттай ижил, хөрвүүлэлтгүй |
| | Different | `USD` | өөр валют, 1 USD = 3000 MNT-өөр хөрвүүлнэ |
| **DailyUsed** (далд орчин) | Empty | өнөөдөр 0 шилжүүлсэн | R = 500,000 |
| | Partly | өнөөдөр 300,000 шилжүүлсэн | R = 200,000 |

**Далд орчин.** Хүсэлтийн дөрвөн талбараас гадна үр дүнд нөлөөлөх далд зүйл бий.

- *Өдрийн лимит хэр дүүрсэн бэ* нь тусдаа категори `DailyUsed` болсон.
- *Үлдэгдэл ба лимитийн харьцаа* нь тусдаа категори биш, `OverBalance`/`OverLimit`-ийн тодорхойлолтод шингэсэн. Хоёр алдааг **тусад нь** шалгахын тулд `OverBalance`-д үлдэгдэл нь R-ээс бага данс (`ACC-POOR`), `OverLimit`-д үлдэгдэл нь R-ээс их данс (`ACC-100`) хэрэгтэй. Баян дансанд `a > B` нь автоматаар `a > R` ч болж, INSUFFICIENT ба LIMIT давхар алдаа үүсгэдэг.
- *Ханш* нь тогтмол stub тул категори биш.

**Currency × Amount.** `Different` үед ижил тоо өөр утгатай. `200 USD` = 600,000 MNT нь лимит 500,000-аас их, харин түүхий `200` нь бага. Хөрвүүлэлтгүйгээр харьцуулдаг алдааг зөвхөн `Different` ба `OverLimit`/`OverBalance` хосолсон тест илрүүлнэ. Хэсэг B-д PICT ийм хосыг заавал хамруулдаг.

### A2: Хязгаарлалтууд — [ERROR], [SINGLE], [IF/property]

`acct` property нь «`from` данс байна» гэсэн үг бөгөөд `From=Valid`, `From=Frozen` өгнө. [ERROR] ба [SINGLE] утга нь зөвхөн нэг тест кейст орж, бусад категори нь default (`Valid`, `Valid`, `Normal`, `Same`, `Empty`) утгатай байна.

| Утга | Тэмдэглэгээ | Үндэслэл (нэг өгүүлбэр) |
| --- | --- | --- |
| From=Valid | `[property acct]` | Байгаа данс үлдэгдэл, лимит, валюттай тул `[if acct]` утгуудыг утга бүхий болгоно. |
| From=Frozen | `[ERROR]` `[property acct]` | ERROR_FROZEN-ийг өөр алдаатай хольж болохгүй, данс нь байгаа тул `acct`-ийг хангана. |
| From=Missing | `[ERROR]` | ERROR_NO_FROM; данс байхгүй тул үлдэгдэл, лимит, валют байхгүй, `[if acct]` утгууд энд хэрэгжихгүй (N/A). |
| To=Valid | — | Хэвийн хүчинтэй хүлээн авагч (default). |
| To=Missing | `[ERROR]` | ERROR_NO_TO нь тусдаа алдааны анги тул өөр алдаатай хольж болохгүй. |
| To=SameAsFrom | `[ERROR]` | ERROR_SAME_ACCOUNT нь `to`, `from` ижил байх хослолоор л гарах тусдаа алдааны анги. |
| Amount=Normal | `[if acct]` | «Хэвийн дүн» нь B, R-тэй харьцуулагдах тул `from` данс байх ёстой. |
| Amount=Zero | `[ERROR]` | ERROR_BAD_AMOUNT-ийн хил (a = 0). |
| Amount=BelowZero | `[ERROR]` | ERROR_BAD_AMOUNT-ийн сөрөг тал, код `== 0` ба `< 0`-ийг тусад нь шалгадаг байж болно. |
| Amount=OverBalance | `[ERROR]` `[if acct]` | ERROR_INSUFFICIENT; үлдэгдэлтэй харьцуулдаг тул `from` данс хэрэгтэй. |
| Amount=OverLimit | `[ERROR]` `[if acct]` | ERROR_LIMIT; хуримтлагдсан лимиттэй харьцуулдаг тул `from` данс хэрэгтэй. |
| Amount=ExactBalance | `[SINGLE]` `[if acct]` | «Үлдэгдлээс хэтрэхгүй» гэдэг нь a = B зөвшөөрөгдөнө гэсэн үг, энэ хилийн off-by-one-ийг нэг удаа шалгахад хангалттай. |
| Currency=Same | `[if acct]` | «`from` дансны валюттай ижил» гэдэг нь данс байгаа үед л утгатай. |
| Currency=Different | `[if acct]` | Хөрвүүлэх валют нь `from` дансны валютаас хамаарна. |
| DailyUsed=Empty | `[if acct]` | Өдрийн лимит нь дансны шинж тул данс байхгүй бол тодорхойгүй. |
| DailyUsed=Partly | `[if acct]` | Өнөөдрийн ашигласан дүн нь тухайн дансных бөгөөд хуримтлагдсан лимитийг шалгах гол орчин. |

**Хязгаарлалтууд ба PICT руу буулгалт**

- **K1 — `[if acct]`.** `From=Missing` үед `[if acct]` утгууд N/A. PICT-д «N/A» утга байхгүй тул default утгаар (`Normal`/`Same`/`Empty`) тогтооно. `Zero`, `BelowZero`-г мөн хасав, учир нь NO_FROM тэдгээрийг ямар ч тохиолдолд далдалдаг.
  `IF [From] = "Missing" THEN [Amount] = "Normal" AND [Currency] = "Same" AND [DailyUsed] = "Empty";`
- **K2 — давуу эрэмбэ.** ERROR_NO_TO нь дүнгийн шалгалтаас өмнө ирдэг тул NO_TO мөрөнд дүнгийн алдаа орвол далдлагдана. Тиймээс багшийн жишээний дагуу `Amount=Normal`.
  `IF [To] = "Missing" THEN [Amount] = "Normal";`
- **Frozen, SameAsFrom-д ижил хязгаарлалтыг зориуд тавиагүй.** Хязгаарлалттай `transfer.model` дотор давхар алдаатай мөр үлдэж, B2-т error masking-ийг ажиглах нөхцөл болно. `~` хувилбар үүнийг хязгаарлалтгүйгээр шийднэ.
- **[ERROR] → PICT.** `transfer-neg.model`-д утгыг `~`-ээр тэмдэглэнэ (B2).
- **[SINGLE] → PICT.** PICT утгыг олон мөрөнд хослуулдаг тул «зөвхөн нэг удаа» гэсэн шинжийг илэрхийлж чадахгүй. `ExactBalance`-ийг PICT модельд оруулаагүй, A3-д гараар нэг спецификаци (T13) болгосон.

**«N → M» тооцоо**

| Алхам | Тооцоо | Тоо |
| --- | --- | --- |
| Бүрэн олонлог | From 3 × To 3 × Amount 6 × Currency 2 × DailyUsed 2 | **216** |
| [IF] хязгаарлалтын дараа (K1, K2) | `From=Missing`: To 3 × 1 × 1 × 1 = 3; `From` ∈ {Valid, Frozen} (2 утга): (`To=Valid` 6·2·2 = 24) + (`To=SameAsFrom` 24) + (`To=Missing` 1·2·2 = 4) = 52 → 2 × 52 = 104; нийт 3 + 104 | **107** |
| [ERROR]/[SINGLE] нэг удаа | ердийн утгуудын хослол 1·1·1·2·2 = 4; [ERROR] утга 8 (Frozen, Missing, To-Missing, SameAsFrom, Zero, BelowZero, OverBalance, OverLimit) тус бүр 1; [SINGLE] 1 | 4 + 8 + 1 = **13** |

**216 → 107 → 13.** Бүрэн олонлогийн 216 хослолоос зөвхөн 13 тест frame үлдэнэ.

### A3: Спецификацийн хүснэгт

17 мөр: A2-ын 13 тест frame (T01–T13) ба 4 давхар нөхцөл (T14–T17). Багана бүр нэг сонголт, сүүлийн багана нь хүлээгдэх үр дүн. Амжилт нь `201 {"result":"OK"}`, оролтын алдаа нь `200 {"result":"ERROR_..."}`.

| ID | Төрөл | From | To | Amount | Currency | DailyUsed | Хүлээгдэх үр дүн |
| --- | --- | --- | --- | --- | --- | --- | --- |
| T01 | Happy path | Valid | Valid | Normal | Same | Empty | 201 `OK` |
| T02 | Happy path | Valid | Valid | Normal | Different | Empty | 201 `OK` |
| T03 | Happy path | Valid | Valid | Normal | Same | Partly | 201 `OK` |
| T04 | Happy path | Valid | Valid | Normal | Different | Partly | 201 `OK` |
| T05 | ERROR | **Frozen** | Valid | Normal | Same | Empty | 200 `ERROR_FROZEN` |
| T06 | ERROR | **Missing** | Valid | Normal | Same | Empty | 200 `ERROR_NO_FROM` |
| T07 | ERROR | Valid | **Missing** | Normal | Same | Empty | 200 `ERROR_NO_TO` |
| T08 | ERROR | Valid | **SameAsFrom** | Normal | Same | Empty | 200 `ERROR_SAME_ACCOUNT` |
| T09 | ERROR | Valid | Valid | **Zero** | Same | Empty | 200 `ERROR_BAD_AMOUNT` |
| T10 | ERROR | Valid | Valid | **BelowZero** | Same | Empty | 200 `ERROR_BAD_AMOUNT` |
| T11 | ERROR | Valid | Valid | **OverBalance** | Same | Empty | 200 `ERROR_INSUFFICIENT` |
| T12 | ERROR | Valid | Valid | **OverLimit** | Same | Empty | 200 `ERROR_LIMIT` |
| T13 | SINGLE | Valid | Valid | **ExactBalance** | Same | Empty | 201 `OK` |
| T14 | Давхар | Frozen | SameAsFrom | Normal | Same | Empty | 200 `ERROR_FROZEN` |
| T15 | Давхар | Valid | Missing | Zero | Same | Empty | 200 `ERROR_NO_TO` |
| T16 | Давхар | Valid | Valid | OverBalance + OverLimit | Same | Empty | 200 `ERROR_INSUFFICIENT` |
| T17 | Давхар | Missing | Valid | Zero | Same | Empty | 200 `ERROR_NO_FROM` |

Тодорхойлолтын шаардлагыг хангасан байдал:

| Шаардлага | Хангасан мөр |
| --- | --- |
| Happy path | T01–T04 |
| Алдааны анги бүр (7) | NO_FROM T06, FROZEN T05, NO_TO T07, SAME_ACCOUNT T08, BAD_AMOUNT T09/T10, INSUFFICIENT T11, LIMIT T12 |
| Дор хаяж нэг SINGLE | T13 |
| Сонирхолтой давхар нөхцөл | T14–T17 |

**Давхар нөхцөлийн мөрүүд.** Хүлээгдэх үр дүнг дээрх давуу эрэмбээр гаргасан. Тодорхойлолт үүнийг шууд заагаагүй.

- **T14:** царцаагдсан данс, мөн `to` нь `from`-той ижил. FROZEN нь SAME_ACCOUNT-аас түрүүлнэ (from → to).
- **T15:** `to` байхгүй, мөн дүн 0. NO_TO нь BAD_AMOUNT-аас түрүүлнэ (to → amount).
- **T16:** `a` нь үлдэгдлээс ч, үлдсэн лимитээс ч их (`ACC-100`, 2,000,000 MNT). INSUFFICIENT нь LIMIT-ээс түрүүлнэ.
- **T17:** `from` байхгүй, мөн дүн 0. NO_FROM нь BAD_AMOUNT-аас түрүүлнэ. Хэрэгжилт дүнгийн шалгалтыг эхэлж хийдэг бол энэ мөр унана, тиймээс давхар мөрүүдээс хамгийн эрсдэлтэй нь юм.

**Тэмдэглэл.** T11, T13 нь `ACC-POOR`, бусад нь `ACC-100` дээр суурилна (A1-ийн fixture). K1, K2 нь T15, T17-г PICT-ийн гаралтаас хасдаг (B2), тиймээс эдгээр давхар мөрийг A3-д гараар хадгалав.

## Хэсэг B: PICT — pairwise covering array

### B1: Лекцийн браузерын жишээ

[browser.model](browser.model) нь лекцийн жишээ: Content, Cookies тус бүр 3 утгатай, Popups, Addons, Attacks, Forgeries тус бүр 2 утгатай, бүрэн олонлог 3·3·2·2·2·2 = **144**.

```text
Content:   Allow, Restrict, Block
Cookies:   Allow, Restrict, Block
Popups:    Yes, No
Addons:    Yes, No
Attacks:   Yes, No
Forgeries: Yes, No
```

Зааврын командууд (`/r` хэрэглээгүй, PICT анхдагчаар ажиллуулах бүрт ижил үр дүн өгдөг):

```bash
pict browser.model | tee results/browser-2way.txt
pict browser.model /o:3 > results/browser-3way.txt
tail -n +2 results/browser-2way.txt | wc -l   # 10
tail -n +2 results/browser-3way.txt | wc -l   # 20
```

| Түвшин | Гаралт | Мөр | Бүрэн олонлогт (144) эзлэх хувь |
| --- | --- | --- | --- |
| 2-way (pairwise) | [results/browser-2way.txt](results/browser-2way.txt) | **10** | 6.9% |
| 3-way (`/o:3`) | [results/browser-3way.txt](results/browser-3way.txt) | **20** | 13.9% |

**2-way: 10 мөр, лекц дээрх гараар хийсэн 9-өөс 1 мөр илүү.** Content ба Cookies хоёр 3 утгатай тул тэдний 3 × 3 = 9 хос бүр өөр өөр мөрөнд байх ёстой, иймээс 9 нь доод хязгаар бөгөөд гараар яг түүнд багтаасан. PICT нь greedy алгоритмтай: мөр бүрийг нэмэхдээ одоогоор хамрагдаагүй хамгийн олон хосыг хамарсан мөрийг сонгодог. Ийм орон нутгийн сонголт нийт хамгийн цөөн мөрийг баталгаажуулдаггүй (хамгийн бага pairwise олонлог олох нь NP-бүрэн бодлого) тул нэг мөр илүү гарсан. Чанар нь алдагдаагүй: 81 хос (`pict browser.model /s` → `Combinations: 81`) бүгд хамрагдсан, зөвхөн хэмнэлт нь оптималаас 1 мөрөөр дутсан.

**3-way: 20 мөр.** Гурвалсан хослол бүр (248 ширхэг) хамрагдах тул 2-way-ээс 2 дахин олон мөр хэрэгтэй боловч бүрэн олонлог 144-ийн 13.9% л байна. Доод хязгаар нь хамгийн том гурван домэйны үржвэр 3 · 3 · 2 = 18, PICT-ийн 20 нь түүнд ойр байна.

Хос ба гурвалсан хослолын хамралтыг PICT-ийн гаралт дээр бие даан шалгасан (хамрагдаагүй хос 0, хамрагдаагүй гурвалсан 0).

### B2: Шилжүүлгийн моделиа PICT-ээр

A1-ийн утгуудыг PICT хэл рүү буулгав. `ExactBalance` ([SINGLE]) нь PICT-д орохгүй (A2), тиймээс 5 параметрийн бүрэн олонлог 3 · 3 · 5 · 2 · 2 = **180**. Гурван model:

**[transfer-nc.model](transfer-nc.model)** — хязгаарлалтгүй, алдааны утга нь энгийн утга:

```text
From:      Valid, Frozen, Missing
To:        Valid, Missing, SameAsFrom
Amount:    Normal, Zero, BelowZero, OverBalance, OverLimit
Currency:  Same, Different
DailyUsed: Empty, Partly
```

**[transfer.model](transfer.model)** — дээрхтэй ижил параметр, дээр нь A2-ын K1, K2:

```text
IF [From] = "Missing" THEN [Amount] = "Normal" AND [Currency] = "Same" AND [DailyUsed] = "Empty";
IF [To] = "Missing" THEN [Amount] = "Normal";
```

**[transfer-neg.model](transfer-neg.model)** — [ERROR] утгууд `~`-тэй, A2-оос зөвхөн N/A хязгаарлалт үлдсэн (шалтгааныг доор тайлбарлав):

```text
From:      Valid, ~Frozen, ~Missing
To:        Valid, ~Missing, ~SameAsFrom
Amount:    Normal, ~Zero, ~BelowZero, ~OverBalance, ~OverLimit
Currency:  Same, Different
DailyUsed: Empty, Partly

IF [From] = "Missing" THEN [Currency] = "Same" AND [DailyUsed] = "Empty";
```

```bash
pict transfer-nc.model  > results/transfer-nc.txt
pict transfer.model     > results/transfer.txt
pict transfer-neg.model > results/transfer-neg.txt
tail -n +2 results/transfer-nc.txt  | wc -l   # 16
tail -n +2 results/transfer.txt     | wc -l   # 14
tail -n +2 results/transfer-neg.txt | wc -l   # 20
```

**Гурван хувилбарын харьцуулалт.** Мөр дэх «алдаа» нь `From≠Valid`, `To≠Valid`, `Amount≠Normal` гурвын тоо (`~Frozen` ≠ `Valid` тул `~` нь тооцоонд саад болохгүй). Happy path нь 0 алдаатай мөр. «Тусдаа шалгагдсан» нь 8 error утгаас яг 1 алдаатай мөрөнд, өөр алдаагүйгээр орсон утгын тоо.

| Хувилбар | Гаралт | Мөр | Happy path | 1 алдаатай | 2 алдаатай | 3 алдаатай | Тусдаа шалгагдсан error утга (8-аас) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `transfer-nc` | [results/transfer-nc.txt](results/transfer-nc.txt) | **16** | 0 | 4 | 6 | 6 | 4 |
| `transfer` | [results/transfer.txt](results/transfer.txt) | **14** | 0 | 4 | 8 | 2 | 4 |
| `transfer-neg` | [results/transfer-neg.txt](results/transfer-neg.txt) | **20** | 4 | 16 | 0 | 0 | 8 |

Хүснэгтийн тоог дараах командаар гаргасан (мөрийн тоо нь дээрх `wc -l`-тэй таарна):

```bash
for m in transfer-nc transfer transfer-neg; do
  rows=$(tail -n +2 results/$m.txt | wc -l)
  awk -F'\t' -v m=$m -v rows=$rows 'NR>1 {e=($1!="Valid")+($2!="Valid")+($3!="Normal"); n[e]++}
    END {printf "%-12s rows=%d happy=%d err1=%d err2=%d err3=%d\n", m, rows, n[0], n[1], n[2], n[3]}' results/$m.txt
done
```

```text
transfer-nc  rows=16 happy=0 err1=4 err2=6 err3=6
transfer     rows=14 happy=0 err1=4 err2=8 err3=2
transfer-neg rows=20 happy=4 err1=16 err2=0 err3=0
```

**Гаралтыг уншсан дүгнэлт** (мөрийн дугаар нь толгойг оролцуулсан файлын мөрийн дугаар):

1. **Happy path мөр гарсангүй.** `transfer-nc`, `transfer`-д (`Valid`, `Valid`, `Normal`) гурвал нэг ч мөрөнд тохиолдоогүй, гэтэл бүх боломжит хос хамрагдсан. Pairwise нь «бүх утга хүчинтэй» мөрийг баталгаажуулдаггүй тул happy path-ийг тусад нь нэмэх хэрэгтэй. `transfer-neg`-д эерэг утгуудаас бүрдсэн 4 мөр (Currency × DailyUsed) автоматаар гарч, бүгд happy path.
2. **Нэг мөрөнд олон алдаа орсон.** `transfer-nc`-д 16 мөрийн 12, `transfer`-д 14 мөрийн 10 нь 2 ба түүнээс олон алдаатай, дээд тал нь 3. K1, K2 нь зөвхөн `Missing`-тэй холбоотой хослолыг хасдаг тул `Frozen`, `SameAsFrom` ба дүнгийн алдаа харилцан хосолсон мөр үлдсэн (A2-д зориуд ингэсэн). `transfer-neg`-д 16 алдаатай мөр бүгд яг 1 алдаатай.
3. **Олон алдаатай мөр юуг шалгаж байгаа нь тодорхойгүй.** Ийм мөрийн хүлээгдэх үр дүн нь давуу эрэмбээр хамгийн түрүүлэх нэг алдаа, бусад нь далдлагдана (error masking). Жишээ нь `Frozen SameAsFrom Zero` мөр зөвхөн FROZEN-ийг шалгаж, SAME_ACCOUNT ба BAD_AMOUNT-ийн талаар юу ч хэлэхгүй. Түүний oracle нь мөн тодорхойлолтод байхгүй, миний тогтоосон давуу эрэмбээс хамаарна.
4. **Зарим алдаа тусдаа шалгагдаагүй.** 1 алдаатай мөрүүдийг `awk -F'\t' 'NR>1 && (($1!="Valid")+($2!="Valid")+($3!="Normal"))==1 {print $1, $2, $3}' results/<файл>.txt` командаар гаргавал:
   - `transfer.txt`: `Missing Valid Normal`, `Valid Valid Zero`, `Valid Valid OverLimit`, `Valid Missing Normal`. Өөрөөр хэлбэл `Frozen`, `SameAsFrom`, `BelowZero`, `OverBalance` зөвхөн өөр алдааны ард орсон тул ERROR_FROZEN, ERROR_SAME_ACCOUNT, ERROR_INSUFFICIENT-ийг цэвэр хэлбэрээр шалгах мөр байхгүй.
   - `transfer-nc.txt`: `Valid Valid Zero`, `Valid Valid OverBalance`, `Frozen Valid Normal`, `Valid Missing Normal`. Энд `Missing` (from), `SameAsFrom`, `BelowZero`, `OverLimit` тусдаа мөргүй.
   - `transfer-neg.txt`: 8 error утга бүгд дор хаяж 1 тусдаа мөртэй.
5. **Greedy `transfer-neg`-д ч харагдана.** `~Zero` 3 мөр (8, 9, 21-р мөр) авсан боловч Currency × DailyUsed-ийг хамрахад 2 мөр хангалттай. 16 алдаатай мөрийн задаргаа: `~Missing` (from) 1, `~Zero` 3, бусад 6 утга тус бүр 2.
6. **Мөрийн тоо.** 16 → 14: K1, K2 нь утгагүй хослолыг хасч 2 мөр хэмнэв. 14 → 20: `~` нь 6 мөр нэмсэн, учир нь error утга бүр Currency × DailyUsed-ийн хоёр утгатай тусад нь хосолж шалгагдах ёстой болсон. Нэмэгдсэн мөр бүр нэг тодорхой алдааг шалгана.

**`~` хэрэглэхэд A2-ын хязгаарлалт хэрэгтэй хэвээр үлдсэн үү?** Хэсэгчлэн.

| A2-ын хязгаарлалт | `~`-тэй үед | Тайлбар |
| --- | --- | --- |
| K2: `To=Missing → Amount=Normal` | Хэрэггүй, хасав | Хоёр `~` утга нэг мөрөнд орохгүй тул NO_TO нь дүнгийн алдаатай холилдож чадахгүй. |
| K1: `From=Missing → Amount=Normal` хэсэг | Хэрэггүй, хасав | Amount-ийн цорын ганц эерэг утга нь `Normal` тул мөрөнд өөр дүн орох боломжгүй. |
| K1: `Currency=Same`, `DailyUsed=Empty` хэсэг (N/A) | **Хэрэгтэй хэвээр, үлдсэн** | `~` нь сөрөг утгуудыг бие биенээс тусгаарладаг, харин сөрөг утгыг утгагүй эерэг утгатай хослуулахаас сэргийлдэггүй. |

Сүүлийн мөрийг баталгаажуулахын тулд `transfer-neg.model`-ийн `IF` мөрийг түр хасч ажиллуулахад `~Missing Valid Normal Different Partly` мөр гарсан: данс байхгүй мөртөө «өөр валют, лимит хэсэгчлэн ашигласан» гэсэн утгагүй мөр. Дүгнэлт: [ERROR]-ийг тусгаарлах хязгаарлалт (K2, K1-ийн Amount хэсэг) `~`-ээр бүрэн орлогдсон, харин [IF] (N/A) төрлийн хязгаарлалт `~`-тэй үед ч хэвээр хэрэгтэй.

**Бодит тест кейс.** Гаралтын 5 мөрийг Лаб 5-ын хэв маягаар (setup + оролт + oracle) тест кейс болгов. Fixture нь A-ийн таамаглалын 3-р зүйл (ханш 1 USD = 3000 MNT, «өнөөдөр 300,000 шилжүүлсэн» нь өмнөх амжилттай шилжүүлгээр бэлдэгдэнэ).

| ID | PICT мөр | Setup | Оролт (`POST /transfer`) | Oracle |
| --- | --- | --- | --- | --- |
| TC1 happy path | `transfer-neg.txt` 5-р мөр: `Valid Valid Normal Different Partly` | `ACC-100` (MNT, үлдэгдэл 1,000,000, лимит 500,000), өнөөдөр 300,000 шилжүүлсэн (R = 200,000); `ACC-200` байгаа | `{"from":"ACC-100","to":"ACC-200","amount":50,"currency":"USD"}` (a = 150,000) | HTTP 201, `{"result":"OK"}` |
| TC2 нэг алдаа | `transfer-neg.txt` 6-р мөр: `Valid Valid ~OverLimit Different Empty` | `ACC-100`, өнөөдөр 0 шилжүүлсэн (R = 500,000); `ACC-200` | `{"from":"ACC-100","to":"ACC-200","amount":200,"currency":"USD"}` (a = 600,000) | HTTP 200, `{"result":"ERROR_LIMIT"}` |
| TC3 нэг алдаа | `transfer-neg.txt` 13-р мөр: `Valid Valid ~OverBalance Same Partly` | `ACC-POOR` (MNT, үлдэгдэл 100,000, лимит 500,000), өнөөдөр 300,000 шилжүүлсэн (R = 200,000); `ACC-200` | `{"from":"ACC-POOR","to":"ACC-200","amount":150000,"currency":"MNT"}` (B < a ≤ R) | HTTP 200, `{"result":"ERROR_INSUFFICIENT"}` |
| TC4 нэг алдаа | `transfer-neg.txt` 14-р мөр: `~Frozen Valid Normal Same Empty` | `ACC-FRZ` (царцаагдсан, MNT, үлдэгдэл 1,000,000, лимит 500,000), өнөөдөр 0; `ACC-200` | `{"from":"ACC-FRZ","to":"ACC-200","amount":150000,"currency":"MNT"}` | HTTP 200, `{"result":"ERROR_FROZEN"}` |
| TC5 олон алдаа | `transfer.txt` 10-р мөр: `Frozen SameAsFrom Zero Same Empty` | `ACC-FRZ` (царцаагдсан, MNT, үлдэгдэл 1,000,000, лимит 500,000), өнөөдөр 0 | `{"from":"ACC-FRZ","to":"ACC-FRZ","amount":0,"currency":"MNT"}` | HTTP 200, `{"result":"ERROR_FROZEN"}` |

- **TC1** нь happy path, **TC2–TC4** нь `transfer-neg`-ийн нэг алдаатай мөр. TC2-т түүхий `200` нь лимит 500,000-аас бага, харин хөрвүүлсний дараа 600,000 болж лимитээс хэтэрнэ, тиймээс хөрвүүлэлтгүйгээр харьцуулдаг хэрэгжилт энд унана. TC3-ын `ACC-POOR` нь `a > B` атлаа `a ≤ R` байлгаж INSUFFICIENT-ийг LIMIT-ээс тусгаарлана. TC4 нь `transfer.txt`-д тусдаа мөргүй FROZEN-ийг цэвэр шалгана.
- **TC5** нь олон алдаатай мөр. Алдаа гурав (FROZEN, SAME_ACCOUNT, BAD_AMOUNT) байх бөгөөд давуу эрэмбээр `NO_FROM → FROZEN → NO_TO → SAME_ACCOUNT → BAD_AMOUNT → ...` тул эхнийх нь ERROR_FROZEN. Хэрэгжилт дүнгийн шалгалтыг эхэлж хийдэг бол TC5 унана. Энэ нь шууд согог гэсэн үг биш, тодорхойлолтыг тодруулах шаардлагатай давуу эрэмбийн таамаглалын зөрүү юм.

### B3: Гараар хийсэн A3 ба PICT-ийн гаралтын харьцуулалт

| Шалгуур | A3: category-partition (гараар) | PICT |
| --- | --- | --- |
| Алдааны семантик | **Илүү сайн.** [ERROR] бүр тусдаа мөртэй (T05–T12), oracle тодорхой. Давхар нөхцөлийг зориуд сонгож давуу эрэмбээр тогтоосон (T14–T17). | Энгийн утгатай бол алдаа холилдоно (`transfer-nc` 16 мөрийн 12, `transfer` 14 мөрийн 10 нь 2+ алдаатай). `~`-тэй бол барина: 16 алдаатай мөр бүгд яг 1 алдаатай. |
| Хосын хамралт | Баталгаагүй. [ERROR] frame-д бусад категори default (`Same`, `Empty`) тул `OverLimit` нь `Different`, `Partly`-тай хэзээ ч хосолдоггүй. | **Баталгаатай.** Боломжит хос бүр дор хаяж нэг мөрөнд бий. `transfer-neg`-д `~OverLimit` нь `Different` (6-р мөр), `Partly` (11-р мөр)-тай хосолсон, TC2-ын хөрвүүлэлтийн урхи эндээс гарсан. |
| Happy path | Заавал бий (T01–T04). | Баталгаагүй: `transfer-nc`, `transfer`-д 0. `~`-тэй бол 4. |
| [SINGLE], давхар нөхцөл | Байгалийн дэмжлэгтэй (T13, T14–T17). | [SINGLE] илэрхийлэгдэхгүй. `~`-тэй гаралтад давхар алдаа огт байхгүй, тиймээс давхар нөхцөлийг гараар нэмнэ. |
| Мөрийн тоо | 13 frame + 4 давхар, 216 хослолоос. | 14–20 мөр, 180 хослолоос. Greedy тул оптимал биш. |
| Oracle | Хүн тогтооно. | Үүсгэхгүй, хүн нэмнэ. |
| Давтагдах чанар | Гараар, үндэслэлийг бичнэ. Утга нэмбэл дахин бодно. | Model-оос автомат, тогтвортой. Утга нэмбэл дахин ажиллуулахад л болно. |

- **Алдааны семантикийг илүү барьдаг нь A3.** Category-partition нь [ERROR], [SINGLE], [IF], давуу эрэмбийг мэддэг. PICT нь зөвхөн `~`-ээр л [ERROR]-ийг ойлгодог бөгөөд бусад тохиолдолд утгыг утгагүй хольж, 8 error утгын 4-ийг тусад нь шалгадаггүй.
- **Хосын хамралтыг баталгаажуулдаг нь PICT.** Хос бүр хамрагдсаныг математикаар баталгаажуулдаг. Гараар хийсэн A3-д ямар хос орхигдсоныг зөвхөн нүдээр л олно.
- **Хамт хэрэглэх нь зөв.** Category-partition-ээр утга, [ERROR]/[SINGLE]/[IF]-ийг тогтооно. PICT-ээр `~`-тэй model ажиллуулж хослолын хамралтыг авна. [SINGLE] ба давхар нөхцөлийг гараар нэмнэ.

## Репо ба илгээх журам

Лабын public репозиторийн холбоос: [Software-engineering-quality-assurance-and-test — lab6](https://github.com/Ulmj7/Software-engineering-quality-assurance-and-test/tree/lab6).

| Файл | Агуулга |
| --- | --- |
| `README.md` | Энэ тайлан |
| `browser.model`, `transfer-nc.model`, `transfer.model`, `transfer-neg.model` | PICT model-ууд |
| `results/pict-build.txt` | PICT-ийн commit ба хэрэглээний заавар (эх кодоос бүтээснийг нотлоно) |
| `results/browser-2way.txt`, `results/browser-3way.txt`, `results/transfer-nc.txt`, `results/transfer.txt`, `results/transfer-neg.txt` | PICT-ийн бүтэн гаралт |
| `.gitignore` | `.DS_Store`, `.idea/`, `pict/`, `build/`, `*.docx` |

README дахь мөрийн тоо бүр дараах командын гаралттай таарна:

```bash
for f in results/browser-2way results/browser-3way results/transfer-nc results/transfer results/transfer-neg; do
  printf '%-26s %s\n' "$f.txt" "$(tail -n +2 $f.txt | wc -l)"
done
```

```text
results/browser-2way.txt   10
results/browser-3way.txt   20
results/transfer-nc.txt    16
results/transfer.txt       14
results/transfer-neg.txt   20
```

PICT-ийн эх код (`~/tools/pict`) болон багшийн зааврын `.docx` файл репод ороогүй. Commit-уудыг ажил хийгдэх явцад үе шат бүрийн дараа тусад нь хийсэн (`git log`): PICT бүтээлт → A1–A2 → A3 → B1 → B2 → B3 ба дүгнэлт.

MS Teams-ийн Assignment-д дээрх **lab6 салбарын линкийг** хугацаанаас өмнө хавсаргаж, **Turn in** товчийг заавал дарна.

## Дүгнэлт

Category-partition нь 216 хослолыг утга учиртай 13 frame болгож, алдаа, хил, давхар нөхцөл, давуу эрэмбийг тодорхой барьсан бол PICT нь хослолын хамралтыг автоматаар, давтагдахаар баталгаажууллаа. A3-д алдаа бүр тусдаа мөртэй, oracle нь тодорхой боловч `OverLimit`-ийг `Different`, `Partly`-тай хосолсон мөр байхгүй, харин PICT-ийн `transfer-neg` энэ хосыг заавал хамруулж TC2-ын хөрвүүлэлтийн урхийг гаргав. Алдааны утгыг энгийн утга шиг бичсэн PICT model семантикийг мэдэхгүй: `transfer-nc`, `transfer` нь happy path мөргүй, 16 мөрийн 12, 14 мөрийн 10 нь олон алдаатай, 8 error утгын 4 нь тусдаа шалгагдаагүй. `~` нь [ERROR] тэмдэглэгээтэй дүйх тул 16 алдаатай мөр бүгд яг 1 алдаатай болж, happy path 4 мөр автоматаар гарсан. Харин `~` нь [IF] төрлийн N/A хязгаарлалтыг орлохгүй, [SINGLE]-д харгалзах зүйл PICT-д байхгүй тул `ExactBalance` ба давхар нөхцөл T14–T17-г гараар үлдээх шаардлагатай болсон. PICT-ийн greedy алгоритм хамгийн цөөн мөрийг баталгаажуулдаггүй: browser жишээнд 9-ийн оронд 10, 3-way-д доод хязгаар 18-ын оронд 20, `~Zero`-д 2-ын оронд 3 мөр гарсан. PICT oracle үүсгэдэггүй тул олон алдаатай мөрийн хүлээгдэх үр дүн миний тогтоосон давуу эрэмбээс хамаарна. Иймээс хоёр аргыг дараалуулж хэрэглэх нь зөв: category-partition-ээр сонголт, [ERROR]/[SINGLE]/[IF]-ийг тогтоож, дараа нь PICT-ээр `~`-тэй model ажиллуулж, [SINGLE] ба давхар нөхцөлийг гараар нэмнэ. Хамгийн их бодол шаардсан алхам нь A2: аль утга [ERROR] вэ, аль хослол N/A вэ, олон алдаанд аль нь түрүүлэх вэ гэдгийг тодорхойлолтод заагаагүй тул өөрөө шийдэх хэрэгтэй байсан. Бусад алхам (PICT model, `~`, тест кейс) бүгд энэ шийдвэрээс хамаарах бөгөөд `~` хэрэглэхэд аль хязгаарлалт үлдэхийг тодруулах нь хамгийн нарийн хэсэг байлаа.
