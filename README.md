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
