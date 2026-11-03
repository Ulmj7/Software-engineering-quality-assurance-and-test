"""Render the saved k6 summaries as reviewable images without changing metrics."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent
RESULTS = ROOT / "results"
IMAGES = ROOT / "screenshots"
FONT = Path(r"C:\Windows\Fonts\consola.ttf")
SYMBOL_FONT = Path(r"C:\Windows\Fonts\seguisym.ttf")
RUNS = {
    "run-05vu": "k6 run --vus 5 --duration 1m script.js",
    "run-30vu": "k6 run --vus 30 --duration 1m script.js",
    "run-100vu": "k6 run --vus 100 --duration 1m script.js",
    "threshold-pass": "k6 run thresholds.js — PASS",
    "threshold-fail": "k6 run thresholds-fail.js — expected FAIL",
}


def summary_lines(source: Path) -> list[str]:
    lines = source.read_text(encoding="utf-8").splitlines()
    first = next(i for i, line in enumerate(lines) if "█ THRESHOLDS" in line or "█ TOTAL RESULTS" in line)
    lines = lines[first:]
    # Windows PowerShell appends a wrapper around k6's expected nonzero exit.
    end = next((i for i, line in enumerate(lines) if line.startswith("k6.exe :")), len(lines))
    return lines[:end]


def main() -> None:
    IMAGES.mkdir(exist_ok=True)
    title_font = ImageFont.truetype(str(FONT), 25)
    body_font = ImageFont.truetype(str(FONT), 18)
    symbol_font = ImageFont.truetype(str(SYMBOL_FONT), 18)
    note_font = ImageFont.truetype(str(FONT), 15)

    for stem, command in RUNS.items():
        source = RESULTS / f"{stem}.txt"
        lines = summary_lines(source)
        canvas = Image.new("RGB", (100, 100))
        draw = ImageDraw.Draw(canvas)
        widest = max(
            draw.textbbox((0, 0), line, font=body_font)[2] for line in lines
        )
        width = max(1120, widest + 80)
        height = 125 + len(lines) * 25 + 28
        image = Image.new("RGB", (width, height), "#111827")
        draw = ImageDraw.Draw(image)
        draw.rounded_rectangle((16, 14, width - 16, height - 14), radius=12, fill="#0b1220", outline="#314158", width=2)
        draw.rectangle((17, 15, width - 17, 78), fill="#1e293b")
        draw.text((42, 25), command, font=title_font, fill="#e5e7eb")
        draw.text((42, 87), f"Source: {source.relative_to(ROOT).as_posix()} (full text output)", font=note_font, fill="#94a3b8")
        y = 119
        for line in lines:
            color = "#e5e7eb"
            if "✓" in line:
                color = "#86efac"
            elif "✗" in line:
                color = "#fca5a5"
            elif "█ " in line:
                color = "#93c5fd"
            special = next((index for index, char in enumerate(line) if char in "✓✗█"), None)
            if special is None:
                draw.text((42, y), line, font=body_font, fill=color)
            else:
                prefix, symbol, suffix = line[:special], line[special], line[special + 1 :]
                sx = 42 + draw.textlength(prefix, font=body_font)
                draw.text((42, y), prefix, font=body_font, fill=color)
                draw.text((sx, y), symbol, font=symbol_font, fill=color)
                draw.text((sx + draw.textlength(symbol, font=body_font), y), suffix, font=body_font, fill=color)
            y += 25
        output = IMAGES / f"{stem}.png"
        image.save(output, optimize=True)
        print(output.relative_to(ROOT))


if __name__ == "__main__":
    main()
