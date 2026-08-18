#!/usr/bin/env python3
"""Emulate the CREASTMT.JCL SORT/REPRO steps that build the TRNXFILE input.

SORT FIELDS=(263,16,CH,A,1,16,CH,A)
OUTREC FIELDS=(1:263,16, 17:1,262, 279:279,50)

Reads app/data/ASCII/dailytran.txt (350-byte TRANSACT records) and writes a
fixed-width sequential file whose layout matches COSTM01.CPY (TRNX-RECORD):
  card number (16) + tran id (16) + TRNX-REST (318).
Also pads the other ASCII inputs to their copybook record lengths.
"""
import sys
from pathlib import Path

ASCII_DIR = Path(sys.argv[1])
OUT_DIR = Path(sys.argv[2])
OUT_DIR.mkdir(parents=True, exist_ok=True)


def records(path, length):
    for raw in path.read_text(encoding="latin-1").splitlines():
        if not raw.strip():
            continue
        yield raw.ljust(length)[:length]


def write_fixed(path, recs, length):
    with path.open("w", encoding="latin-1", newline="") as fh:
        for rec in recs:
            fh.write(rec.ljust(length)[:length] + "\n")


# TRNXFILE: reorder + sort, then pad to the 350-byte FD-TRNXFILE-REC length.
trnx = []
for rec in records(ASCII_DIR / "dailytran.txt", 350):
    card = rec[262:278]
    head = rec[0:262]
    tail = rec[278:328]
    trnx.append(card + head + tail)
trnx.sort(key=lambda r: (r[0:16], r[16:32]))
write_fixed(OUT_DIR / "trxfl.dat", trnx, 350)

write_fixed(OUT_DIR / "xref.dat", records(ASCII_DIR / "cardxref.txt", 50), 50)
write_fixed(OUT_DIR / "cust.dat", records(ASCII_DIR / "custdata.txt", 500), 500)
write_fixed(OUT_DIR / "acct.dat", records(ASCII_DIR / "acctdata.txt", 300), 300)
print("prepared %d transaction records" % len(trnx))
