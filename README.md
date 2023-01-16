# Connect 4
## Aktueller Stand

Stand jetzt habe ich einen funktionierenden Min-Max und Alpha-Beta.

### Funktionalitäten:
- Die isWinning Methode ist korrekt und mittels for-loops umgesetzt
- Der Code ist mit einigen Kommentaren zum Verständnis versehen.
- Beim Alpha-Beta wird die benötigte Zeit sowie die Anzahl cut-offs angezeigt. 
- Es sind mehrere JUnit-Tests vorhanden (Greedy vs. AlphaBeta / AlphaBeta vs. MinMax / AlphaBeta vs. AlphaBeta)
- Dank einem kleinen Hack, indem der Alpha Beta die possibleMoves sortiert, wurde die Performance viel schneller als zuvor.
## Probleme

Leider hatte ich lange Schwierigkeiten mit dem AlphaBeta. Er spielte ungefähr so wie ein GreedyPlayer und ich wusste nicht wieso. Herr von Känels Kommentar in Teams hat mir dann weitergeholfen. 

## Ausblick

Wenn ich noch mehr Zeit gehabt hätte, würde ich weitere optimierungen anfangen.

## Selbsteinschätzung
5.5