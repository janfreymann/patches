<CsoundSynthesizer>
<CsOptions>

</CsOptions>

<CsInstruments>
; example written by Iain McCurdy

sr      =       44100
ksmps   =       32
nchnls  =       4
instr 1
aA0, aA1 diskin2 "a.wav", 1, 0, 0
aB0 sum (0.0467693205649989 * aA0), (0.0467693205649989 * aA1)
aB1 sum (0.0371508088537151 * aA0), (0.997118387509373 * aA1)
aB2 sum (0.997118387509373 * aA0), (0.0371508088537151 * aA1)
aB3 sum (0.0467693205649989 * aA0), (0.0467693205649989 * aA1)
outs aB0, aB1, aB2, aB3
  endin

</CsInstruments>

<CsScore>
i 1 0 150
e
</CsScore>

</CsoundSynthesizer>
