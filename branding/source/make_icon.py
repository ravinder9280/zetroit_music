"""Zetroit Music icon, traced from the supplied design (85x102 px screenshot).

All coordinates below are in that screenshot's pixels: a white rounded-square outline
(outer box 1,11 to 83,93) around a line-art eighth note whose head is a ring with a dot.
Every output is generated from these shapes so they all match.
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

BLACK = (0, 0, 0)
WHITE = (255, 255, 255)

OUTER = (1.0, 11.0, 83.0, 93.0)      # outline, outer edge
STROKE = 4.0


def circle(cx, cy, r, steps=64):
    return [(cx + r * math.cos(2 * math.pi * i / steps), cy + r * math.sin(2 * math.pi * i / steps)) for i in range(steps)]


def rounded_rect(x0, y0, x1, y1, r, steps=16):
    pts = []
    for cx, cy, a0 in ((x1 - r, y0 + r, -90), (x1 - r, y1 - r, 0), (x0 + r, y1 - r, 90), (x0 + r, y0 + r, 180)):
        pts += [(cx + r * math.cos(math.radians(a0 + 90 * i / steps)), cy + r * math.sin(math.radians(a0 + 90 * i / steps)))
                for i in range(steps + 1)]
    return pts


def capsule_v(x, y0, y1, h, steps=12):
    top = [(x + h * math.cos(math.pi + math.pi * i / steps), y0 + h * math.sin(math.pi + math.pi * i / steps)) for i in range(steps + 1)]
    bottom = [(x + h * math.cos(math.pi * i / steps), y1 + h * math.sin(math.pi * i / steps)) for i in range(steps + 1)]
    return top + bottom


def flag():
    # Leaves the top of the stem at about 32 degrees, then drops straight down to a round end
    cap = [(59 + 2 * math.cos(math.pi * i / 10), 54 + 2 * math.sin(math.pi * i / 10)) for i in range(11)]
    return [(40, 30.5), (61, 43.5)] + cap + [(57, 45.7), (44, 37.7), (40, 35.2)]


# Each element is a list of subpaths filled with the even-odd rule (rings have holes)
OUTLINE = [rounded_rect(*OUTER, 20.5), rounded_rect(OUTER[0] + STROKE, OUTER[1] + STROKE, OUTER[2] - STROKE, OUTER[3] - STROKE, 16.5)]
GLYPH = [
    # stem, the right edge of the note head; its top is cut along the flag so they meet in a point
    [[(40, 30.5), (44, 32.98), (44, 64.2), (40, 64.2)]],
    [flag()],
    [circle(33.9, 64.25, 10.3), circle(33.9, 64.25, 6.2)],   # note head ring
    [circle(34.0, 64.1, 3.0)],                          # dot in the middle of the head
    [capsule_v(50.5, 49.6, 66.6, 1.9)],                 # short bar beside the stem
    [circle(59.0, 59.4, 2.0)],                          # dot under the flag
]
GLYPH_BOX = (23.6, 30.5, 61.0, 74.55)


def place(elements, src_box, dst_box):
    """Maps elements from screenshot pixels into a target square, keeping proportions."""
    sx0, sy0, sx1, sy1 = src_box
    dx0, dy0, dx1, dy1 = dst_box
    k = min((dx1 - dx0) / (sx1 - sx0), (dy1 - dy0) / (sy1 - sy0))
    ox = dx0 + ((dx1 - dx0) - (sx1 - sx0) * k) / 2
    oy = dy0 + ((dy1 - dy0) - (sy1 - sy0) * k) / 2
    return [[[(ox + (x - sx0) * k, oy + (y - sy0) * k) for x, y in sub] for sub in el] for el in elements]


def full_icon(side, size):
    """The note alone, its height being `side`, centered in a `size` canvas."""
    return glyph_only(side, size)


def glyph_only(side, size):
    m = (size - side) / 2
    return place(GLYPH, GLYPH_BOX, (m, m, m + side, m + side))


# ---------- raster ----------

def mask(elements, size, ss=4):
    out = Image.new("L", (size * ss, size * ss), 0)
    for el in elements:
        layer = Image.new("L", out.size, 0)
        d = ImageDraw.Draw(layer)
        for i, sub in enumerate(el):
            d.polygon([(x * ss, y * ss) for x, y in sub], fill=0 if i else 255)
        out = Image.composite(layer, out, layer)
    return out.resize((size, size), Image.LANCZOS)


def render(elements, size, background="square"):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    if background:
        bg = Image.new("L", (size * 4, size * 4), 0)
        d = ImageDraw.Draw(bg)
        full = size * 4 - 1
        if background == "circle":
            d.ellipse((0, 0, full, full), fill=255)
        elif background == "rounded":
            d.rounded_rectangle((0, 0, full, full), radius=size * 4 * 0.22, fill=255)
        else:
            d.rectangle((0, 0, full, full), fill=255)
        img.paste(BLACK + (255,), (0, 0), bg.resize((size, size), Image.LANCZOS))
    img.paste(WHITE + (255,), (0, 0), mask(elements, size))
    return img


# ---------- vector ----------

def path_data(el, scale=1.0):
    return " ".join("M" + " L".join(f"{x * scale:.2f},{y * scale:.2f}" for x, y in sub) + " Z" for sub in el)


def vector_xml(elements, viewport, size_dp, color="#FFFFFFFF"):
    paths = "\n".join(
        f'    <path android:fillColor="{color}" android:fillType="evenOdd"\n        android:pathData="{path_data(el)}" />'
        for el in elements)
    return f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{size_dp}dp"
    android:height="{size_dp}dp"
    android:viewportWidth="{viewport}"
    android:viewportHeight="{viewport}">
{paths}
</vector>
'''


def svg(elements, size, background=True):
    bg = f'  <rect width="{size}" height="{size}" fill="#000000"/>\n' if background else ""
    paths = "\n".join(f'    <path fill-rule="evenodd" d="{path_data(el)}"/>' for el in elements)
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">
{bg}  <g fill="#FFFFFF">
{paths}
  </g>
</svg>
'''


def font(px):
    for path in ("/usr/share/fonts/truetype/ubuntu/Ubuntu-B.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"):
        if os.path.exists(path):
            return ImageFont.truetype(path, px)
    return ImageFont.load_default()


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    if isinstance(data, Image.Image):
        data.save(path)
    else:
        open(path, "w").write(data)


def generate(res, art):
    """res: the app's res folder; art: folder for artwork outside the app."""
    # Adaptive icon (Android 8+): 108dp canvas. A 54dp outline keeps its rounded corners
    # inside the 66dp safe circle, so no launcher shape clips it.
    write(f"{res}/drawable/ic_launcher_foreground.xml", vector_xml(full_icon(42, 108), 108, 108))
    write(f"{res}/drawable/ic_launcher_monochrome.xml", vector_xml(full_icon(42, 108), 108, 108))
    for name in ("ic_launcher", "ic_launcher_round"):
        write(f"{res}/mipmap-anydpi-v26/{name}.xml", '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
''')
    # Notification and About screen: the note on its own; the outline is too heavy at 24dp
    write(f"{res}/drawable/app_icon.xml", vector_xml(glyph_only(19, 24), 24, 24))

    # Launcher icons for Android 7 and older
    for folder, px in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
        big = px * 4
        write(f"{res}/mipmap-{folder}/ic_launcher.png",
              render(full_icon(big * 0.56, big), big, "rounded").resize((px, px), Image.LANCZOS))
        write(f"{res}/mipmap-{folder}/ic_launcher_round.png",
              render(full_icon(big * 0.50, big), big, "circle").resize((px, px), Image.LANCZOS))

    # Android TV banner, 320x180 at xhdpi
    w, h, ss = 320, 180, 4
    banner = Image.new("RGBA", (w * ss, h * ss), BLACK + (255,))
    logo = render(full_icon(80 * ss, 96 * ss), 96 * ss, None)
    banner.alpha_composite(logo, (28 * ss, 42 * ss))
    d = ImageDraw.Draw(banner)
    d.text((142 * ss, 58 * ss), "Zetroit", font=font(36 * ss), fill=WHITE)
    d.text((142 * ss, 98 * ss), "Music", font=font(28 * ss), fill=(150, 165, 200))
    write(f"{res}/mipmap-xhdpi/ic_banner.png", banner.convert("RGB").resize((w, h), Image.LANCZOS))

    # Artwork outside the app
    write(f"{art}/play_store_512.png", render(full_icon(512 * 0.6, 512), 512, "square").convert("RGB"))
    write(f"{art}/logo_1024.png", render(full_icon(1024 * 0.6, 1024), 1024, "square"))
    write(f"{art}/logo_glyph_1024.png", render(glyph_only(1024 * 0.8, 1024), 1024, None))
    write(f"{art}/logo.svg", svg(full_icon(512 * 0.6, 512), 512))
    write(f"{art}/logo_glyph_white.svg", svg(glyph_only(512, 512), 512, background=False))


if __name__ == "__main__":
    generate(sys.argv[1], sys.argv[2])
