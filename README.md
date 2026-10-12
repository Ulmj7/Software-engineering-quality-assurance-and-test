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
