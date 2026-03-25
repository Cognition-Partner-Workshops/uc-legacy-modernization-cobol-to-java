      ******************************************************************
      * Shared copybook for POPULATE-HEADER-INFO paragraph body.
      * Populates screen header fields: titles, tran name, program
      * name, current date (MM/DD/YY), and current time (HH:MM:SS).
      *
      * Usage: COPY CSHDR01Y REPLACING ==MAPOUT== BY ==<map-output>==.
      *   where <map-output> is the BMS output map (e.g. COMEN1AO).
      ******************************************************************
           MOVE FUNCTION CURRENT-DATE  TO WS-CURDATE-DATA

           MOVE CCDA-TITLE01           TO TITLE01O OF MAPOUT
           MOVE CCDA-TITLE02           TO TITLE02O OF MAPOUT
           MOVE WS-TRANID              TO TRNNAMEO OF MAPOUT
           MOVE WS-PGMNAME             TO PGMNAMEO OF MAPOUT

           MOVE WS-CURDATE-MONTH       TO WS-CURDATE-MM
           MOVE WS-CURDATE-DAY         TO WS-CURDATE-DD
           MOVE WS-CURDATE-YEAR(3:2)   TO WS-CURDATE-YY

           MOVE WS-CURDATE-MM-DD-YY    TO CURDATEO OF MAPOUT

           MOVE WS-CURTIME-HOURS       TO WS-CURTIME-HH
           MOVE WS-CURTIME-MINUTE      TO WS-CURTIME-MM
           MOVE WS-CURTIME-SECOND      TO WS-CURTIME-SS

           MOVE WS-CURTIME-HH-MM-SS    TO CURTIMEO OF MAPOUT
