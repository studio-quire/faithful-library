"""The Faithful Library's card for the site: both editions' seals over a frame, Still Faithful's on the left, each pixel-sharp
in a smooth glow of its own colour. The glow belongs to the card and the link preview; the packs' icons are the plain seals.

    python tools/library_card.py <frame.png> <out.png> [x1,y1,x2,y2]   at those centres, or CENTRES
    python tools/library_card.py colours                               the glows' colours, for the link preview's CSS

Claude Opus 5.5, 2026-11-05.
"""
import colorsys
import sys
from pathlib import Path

from PIL import Image, ImageFilter

FINAL = Path(__file__).resolve().parent.parent / "prism/release/icons/final"
W, H = 1120, 700
EDITIONS = ["stillfaithful", "oldfaithful"]
# Side by side in the sky, leaving the bay and the cliffs to be seen.
CENTRES = [(148, 148), (398, 148)]
SEAL = 208  # 6.5 times the 32 grid, drawn from the 512
GLOW = [(14, 1.6), (52, 1.4)]  # blur radius and strength: a close bright edge inside a wide falloff
# Violet sits close to the card's periwinkle sky, so its glow is deeper and stronger than green's to show as much.
LIGHTNESS = {"stillfaithful": 0.72, "oldfaithful": 0.8}
BOOST = {"stillfaithful": 1.3, "oldfaithful": 1.0}


def seal(edition):
    return Image.open(FINAL / f"{edition}-512.png").convert("RGBA")


def glow_colour(edition):
    """The seal's own hue, lightened: the mean of its saturated pixels."""
    pixels = [(r, g, b) for r, g, b, a in seal(edition).getdata() if a > 200 and max(r, g, b) - min(r, g, b) > 50]
    mean = [sum(p[i] for p in pixels) / len(pixels) / 255 for i in range(3)]
    h, _, _ = colorsys.rgb_to_hls(*mean)
    return tuple(round(c * 255) for c in colorsys.hls_to_rgb(h, LIGHTNESS[edition], 0.85))


def add_seal(card, edition, centre):
    # By nearest neighbour, like the other cards' seals: 16 source pixels to a grid cell.
    big = seal(edition).resize((SEAL, SEAL), Image.NEAREST)
    left, top = centre[0] - SEAL // 2, centre[1] - SEAL // 2
    mask = Image.new("L", (W, H), 0)
    mask.paste(big.getchannel("A"), (left, top))
    colour = Image.new("RGB", (W, H), glow_colour(edition))
    for radius, strength in reversed(GLOW):
        alpha = mask.filter(ImageFilter.GaussianBlur(radius)).point(lambda v: min(255, int(v * strength * BOOST[edition])))
        card = Image.composite(colour, card, alpha)
    card.paste(big, (left, top), big)
    return card


def main():
    if sys.argv[1] == "colours":
        for e in EDITIONS:
            print(e, glow_colour(e))
        return
    centres = CENTRES
    if len(sys.argv) > 3:
        x1, y1, x2, y2 = map(int, sys.argv[3].split(","))
        centres = [(x1, y1), (x2, y2)]
    card = Image.open(sys.argv[1]).convert("RGB").resize((W, H), Image.LANCZOS)
    for edition, centre in zip(EDITIONS, centres):
        card = add_seal(card, edition, centre)
    card.save(sys.argv[2], optimize=True)


if __name__ == "__main__":
    main()
