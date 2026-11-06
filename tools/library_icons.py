"""The library packs' icons: each edition's β seal, untouched, with a second frame around it that marks the version with mods.

    python tools/library_icons.py candidates   frames to compare, a sheet at real icon sizes and on cards, into local/icons/library
    python tools/library_icons.py final        the chosen frame, four times the grid, into prism/release/icons/final/faithfullibrary-<edition>.png
    python tools/library_icons.py card <frame.png> <out.png> [frame-name]   the library's card: both framed seals over a frame

Drawn on the seals' own grid, from final/<edition>-32.png, whose 32 x 32 pixels stay as they are. A frame adds pixels around them.

Claude Opus 5.5, 2026-11-05.
"""
import colorsys
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
FINAL = ROOT / "prism/release/icons/final"
EDITIONS = {"old": "oldfaithful", "still": "stillfaithful"}
CHOSEN = "ring-gap"
SEAL_R = 15.0  # the seal's outer edge, about the centre of its 32 x 32
SHEET_W, CARD_W, CARD_H = 1500, 1120, 700
SEAL_ON_CARD = 300  # the family's cards draw a 512 seal this wide


def seal(edition):
    return Image.open(FINAL / f"{EDITIONS[edition]}-32.png").convert("RGBA")


def mean_hue(im):
    """The seal's own colour: the mean of its saturated pixels."""
    rs = gs = bs = n = 0
    for r, g, b, a in im.getdata():
        if a > 200 and max(r, g, b) - min(r, g, b) > 50:
            rs, gs, bs, n = rs + r, gs + g, bs + b, n + 1
    return rs / n, gs / n, bs / n


def tint(rgb, lightness, saturation):
    h, _, _ = colorsys.rgb_to_hls(*(c / 255 for c in rgb))
    return tuple(round(c * 255) for c in colorsys.hls_to_rgb(h, lightness, saturation))


def outline(mask):
    """The pixels just outside a set of pixels, a line one pixel wide that follows its shape."""
    size = len(mask)
    out = [[False] * size for _ in range(size)]
    for y in range(size):
        for x in range(size):
            if not mask[y][x] and any(0 <= y + b < size and 0 <= x + a < size and mask[y + b][x + a] for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out[y][x] = True
    return out


def grown(mask):
    """The set with its 8 neighbours, for the gap."""
    size = len(mask)
    return [[any(0 <= y + b < size and 0 <= x + a < size and mask[y + b][x + a] for a in (-1, 0, 1) for b in (-1, 0, 1)) for x in range(size)] for y in range(size)]


def ring(seal_on_canvas, color, gaps=0, away=1):
    """A line following the seal's edge, with {@code away} pixels clear between; with gaps, a stitched line."""
    size = seal_on_canvas.width
    alpha = seal_on_canvas.getchannel("A")
    mask = [[alpha.getpixel((x, y)) > 0 for x in range(size)] for y in range(size)]
    for _ in range(away):
        mask = grown(mask)
    line = outline(mask)
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            if line[y][x]:
                if gaps and int((math.atan2(y - c, x - c) + math.pi) / (2 * math.pi) * gaps * 2) % 2:
                    continue
                im.putpixel((x, y), (*color, 255))
    return im


def on_canvas(im, size):
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    at = (size - im.width) // 2
    out.alpha_composite(im, (at, at))
    return out


def glow(edition, grid, color, soft):
    """A halo in steps of the grid's pixels, or smooth at 16 times the grid when soft."""
    scale = 16 if soft else 1
    size = grid * scale
    base = on_canvas(seal(edition), grid)
    mask = base.getchannel("A").resize((size, size), Image.NEAREST)
    spread = mask.filter(ImageFilter.GaussianBlur(1.6 * scale))
    halo = Image.new("RGBA", (size, size), (*color, 0))
    halo.putalpha(spread.point(lambda v: min(255, int(v * 1.0))))
    if soft:
        out = base.resize((size, size), Image.NEAREST)
        under = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        under.alpha_composite(halo)
        under.alpha_composite(out)
        return under
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.alpha_composite(halo)
    out.alpha_composite(base)
    return out


# Each: what it is, and a drawing from an edition. Returned on its own grid; soft ones are at 16 times it, marked by their size.
def framed(e, size, color, gaps=0, away=1):
    im = on_canvas(seal(e), size)
    under = ring(im, color, gaps, away)
    under.alpha_composite(im)
    return under


def f_ring_touch(e):
    # A pearl line right on the seal's edge.
    return framed(e, 34, (232, 236, 243), away=0)


def f_ring_gap(e):
    return framed(e, 36, tint(mean_hue(seal(e)), 0.78, 0.45))


def f_ring_silver(e):
    return framed(e, 36, (214, 220, 230))


def f_ring_double(e):
    im = on_canvas(seal(e), 40)
    under = ring(im, tint(mean_hue(seal(e)), 0.72, 0.45))
    under.alpha_composite(ring(im, tint(mean_hue(seal(e)), 0.86, 0.45), away=3))
    under.alpha_composite(im)
    return under


def f_stitched(e):
    # A quire is sewn: a line of stitches.
    return framed(e, 36, (232, 236, 243), gaps=18)


def f_glow_stepped(e):
    return glow(e, 40, tint(mean_hue(seal(e)), 0.78, 0.7), False)


def f_glow_soft(e):
    return glow(e, 40, tint(mean_hue(seal(e)), 0.78, 0.7), True)


def f_glow_pearl(e):
    return glow(e, 40, (244, 247, 252), True)


FRAMES = {
    "ring-touch": ("a pearl line hugging the seal", f_ring_touch),
    "ring-gap": ("a thin ring of the seal's own colour, a pixel away", f_ring_gap),
    "ring-silver": ("the same in the family's silver", f_ring_silver),
    "ring-double": ("two thin rings", f_ring_double),
    "stitched": ("a line of stitches", f_stitched),
    "glow-stepped": ("a glow in the seal's colour, in the grid's pixels", f_glow_stepped),
    "glow-soft": ("the same glow, smooth", f_glow_soft),
    "glow-pearl": ("a smooth pearl glow", f_glow_pearl),
}


def at_size(im, size):
    """As a launcher shows it: a soft one smoothly, a pixel one by whole pixels where it can be."""
    return im.resize((size, size), Image.LANCZOS if im.width > 100 or size < im.width else Image.NEAREST)


def on_card(frame_img, images, centres, seal_px):
    """The family's card: the frame, each image pixel-sharp with its 32-grid at the seal's size, under a soft shadow."""
    card = frame_img.convert("RGB").resize((CARD_W, CARD_H), Image.LANCZOS)
    for im, (cx, cy) in zip(images, centres):
        grid = im.width if im.width <= 100 else im.width // 16
        px = round(seal_px * grid / 32)
        big = im.resize((px, px), Image.NEAREST if im.width <= 100 else Image.LANCZOS)
        left, top = cx - px // 2, cy - px // 2
        alpha = Image.new("L", (CARD_W, CARD_H), 0)
        alpha.paste(big.getchannel("A"), (left, top))
        shadow = alpha.filter(ImageFilter.GaussianBlur(24)).point(lambda v: int(v * 0.24))
        card = Image.composite(Image.new("RGB", (CARD_W, CARD_H), (0, 0, 0)), card, shadow)
        card.paste(big, (left, top), big)
    return card


# Two seals where the family has one, a little smaller, side by side on the wall, leaving the corridor and the spawner to be seen.
CARD_CENTRES = [(160, 300), (408, 300)]
CARD_SEAL = 208


def candidates(out):
    out.mkdir(parents=True, exist_ok=True)
    frame_path = ROOT.parent / "quire-site/lab/covers/faithful-library-view.png"
    frame_img = Image.open(frame_path) if frame_path.exists() else Image.new("RGB", (1440, 900), (90, 90, 90))
    backgrounds = [("dark", (45, 47, 52)), ("light", (240, 241, 244))]
    sizes = (32, 48, 64, 128)
    row_h = 300
    sheet = Image.new("RGB", (SHEET_W, 90 + row_h * (len(FRAMES) + 1)), (250, 250, 252))
    d = ImageDraw.Draw(sheet)
    d.text((20, 16), "Library pack icons: the seal unchanged, a frame around it. Prism's 32, 48, 64 and 128 on dark and light, each edition; then a card.", fill=(30, 30, 40))
    rows = [("plain seals, for reference", None)] + [(f"{k}: {v[0]}", v[1]) for k, v in FRAMES.items()]
    for i, (label, draw) in enumerate(rows):
        y = 60 + i * row_h
        d.text((20, y), label, fill=(30, 30, 40))
        x = 20
        for e in EDITIONS:
            im = draw(e) if draw else seal(e)
            if draw:
                im.save(out / f"{label.split(':')[0]}-{e}.png", optimize=True)
            for name, bg in backgrounds:
                panel = Image.new("RGB", (300, 136), bg)
                px = 4
                for s in sizes:
                    panel.paste(at_size(im, s), (px, 136 - s - 4), at_size(im, s))
                    px += s + 6
                sheet.paste(panel, (x, y + 20))
                x += 310
                if x > 1100:
                    x = 20
        if draw:
            card = on_card(frame_img, [draw("old"), draw("still")], CARD_CENTRES, CARD_SEAL)
            card.save(out / f"{label.split(':')[0]}-card.png", optimize=True)
            sheet.paste(card.resize((208, 130), Image.LANCZOS), (1270, y + 24))
    sheet.save(out / "sheet.png", optimize=True)


def final():
    for e in EDITIONS:
        im = FRAMES[CHOSEN][1](e)
        im.resize((im.width * 4, im.height * 4), Image.NEAREST).save(FINAL / f"faithfullibrary-{e}.png", optimize=True)


def main():
    what = sys.argv[1]
    if what == "candidates":
        candidates(ROOT / "local/icons/library")
    elif what == "final":
        final()
    elif what == "card":
        name = sys.argv[4] if len(sys.argv) > 4 else CHOSEN
        draw = FRAMES[name][1]
        on_card(Image.open(sys.argv[2]), [draw("old"), draw("still")], CARD_CENTRES, CARD_SEAL).save(sys.argv[3], optimize=True)


if __name__ == "__main__":
    main()
