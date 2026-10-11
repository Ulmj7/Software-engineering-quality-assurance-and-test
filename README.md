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
