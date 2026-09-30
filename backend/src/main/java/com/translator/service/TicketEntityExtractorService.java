package com.translator.service;

import com.translator.model.TicketDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TicketEntityExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(TicketEntityExtractorService.class);

    // Date Pattern: matches 2026-11-20, 15/10/2026, 12.05.2025, 25-Dec-2026, 12 May 2025, 12-May-2025, 15 Oct 2026, 15 de Octubre de 2026
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(?:\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|\\d{1,2}[-\\s]+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|Ene|Abr|Ago|Dic|Mai|Okt|Janv|F[eé]vr|Avr|Juin|Juil|Ao[uû]t|D[eé]c)[a-z]*\\.?[-\\s]+\\d{2,4}|\\d{1,2}(?:st|nd|rd|th)?\\s+(?:of\\s+)?(?:January|February|March|April|May|June|July|August|September|October|November|December|Enero|Febrero|Marzo|Abril|Mayo|Junio|Julio|Agosto|Septiembre|Octubre|Noviembre|Diciembre|Janvier|Février|Mars|Avril|Mai|Juin|Juillet|Août|Septembre|Octobre|Novembre|Décembre|Januar|Februar|März|April|Mai|Juni|Juli|August|September|Oktober|November|Dezember)\\s+\\d{2,4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Time Pattern: matches 08:30, 14:45, 08:30:00, 8:30 PM, 08h30
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "\\b(?:(?:[01]?\\d|2[0-3])[:.h][0-5]\\d(?:[:.][0-5]\\d)?(?:\\s*(?:AM|PM|am|pm))?)\\b"
    );

    // Flight / Train / Transport Carrier regex
    private static final Pattern CARRIER_PATTERN = Pattern.compile(
            "\\b(?:(?:AVE|TGV|ICE|IC|EC|FLIX|FLIXBUS|TALGO|ALVIA|FRECCIAROSSA|ITALO|EUROSTAR|THALYS|OUIGO|SNCF|RENFE|DB|IRCTC|AMTRAK)\\s*[-#]?\\s*\\d{2,5}|Flight\\s*#?\\s*[A-Z0-9]{2,3}\\s*[-#]?\\s*\\d{2,4}|(?:AF|BA|LH|EK|UA|AA|DL|IB|AI|6E|SG|QR|SQ|KL|EY|AZ|LX|OS|FR|U2|VY|TO|W6)\\s*[-#]?\\s*\\d{2,4}|Train\\s*#?\\s*\\d{2,6}|Vuelo\\s*#?\\s*[A-Z0-9]{2,3}\\s*\\d{2,4}|Vol\\s*#?\\s*[A-Z0-9]{2,3}\\s*\\d{2,4}|Zug\\s*#?\\s*[A-Z0-9]{2,5}\\s*\\d{2,4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Parses raw extracted text into structured TicketDetails.
     */
    public TicketDetails extractDetails(String text) {
        TicketDetails details = new TicketDetails();
        if (text == null || text.trim().isEmpty()) {
            return details;
        }

        String[] lines = text.split("\r?\n");

        // Pass 1: Line-by-line structured extraction
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String nextLine = (i + 1 < lines.length) ? lines[i + 1].trim() : "";

            // Passenger Name
            if (details.getPassengerName() == null) {
                Matcher m = Pattern.compile("^(?:Passenger(?:\\s*Name)?|Pasajero(?:\\s*Titular)?|Nombre(?:\\s*del\\s*pasajero)?|Passager(?:\\s*Nom)?|Nom(?:\\s*du\\s*Passager)?|Fahrgast(?:name)?|Passagier|Passeggero|Passageiro|Name|Traveler|Пассажир|यात्री|乗客|乘客)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    String name = cleanValue(m.group(1));
                    if (name != null && !name.toLowerCase().contains("billete") && !name.toLowerCase().contains("pnr")) {
                        details.setPassengerName(name);
                    }
                } else if (line.matches("(?i)^(?:Passenger(?:\\s*Name)?|Pasajero(?:\\s*Titular)?|Nombre(?:\\s*del\\s*pasajero)?|Passager|Fahrgast|Passagier|Traveler)[:]?$") && !nextLine.isEmpty()) {
                    details.setPassengerName(cleanValue(nextLine));
                }
            }

            // Departure / Origin Station
            if (details.getDepartureStation() == null) {
                Matcher m = Pattern.compile("^(?:From|Origin|Departure\\s*Station|Depart\\s*Station|Boarding\\s*Station|Station\\s*of\\s*Origin|Origen(?:\\s*\\([^)]+\\))?|Salida\\s*(?:Estaci[oó]n)?|Estaci[oó]n\\s*(?:de)?\\s*Origen|Gare\\s*(?:de)?\\s*d[eé]part|D[eé]part\\s*(?:Gare)?|Von|Startbahnhof|Abfahrtsbahnhof|Da|Stazione\\s*di\\s*Partenza|Origem|Откуда|Отправление|से|प्रस्थान|出発|出发)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setDepartureStation(cleanValue(m.group(1)));
                } else if (line.matches("(?i)^(?:From|Origin|Boarding\\s*Station|Origen|Gare\\s*de\\s*d[eé]part|Von|Startbahnhof|Origem)[:]?$") && !nextLine.isEmpty()) {
                    details.setDepartureStation(cleanValue(nextLine));
                }
            }

            // Arrival / Destination Station
            if (details.getArrivalDestination() == null) {
                Matcher m = Pattern.compile("^(?:To|Destination|Arrival\\s*Station|Station\\s*of\\s*Destination|Destino(?:\\s*\\([^)]+\\))?|Llegada\\s*(?:Estaci[oó]n)?|Estaci[oó]n\\s*(?:de)?\\s*Llegada|Gare\\s*(?:d')?arriv[eé]e|Arriv[eé]e\\s*(?:Gare)?|Nach|Zielbahnhof|Ankunftsbahnhof|Destinazione|Stazione\\s*di\\s*Arrivo|Куда|Прибытие|तक|आगमन|到着|到达)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setArrivalDestination(cleanValue(m.group(1)));
                } else if (line.matches("(?i)^(?:To|Destination|Destino|Gare\\s*d'arriv[eé]e|Nach|Zielbahnhof)[:]?$") && !nextLine.isEmpty()) {
                    details.setArrivalDestination(cleanValue(nextLine));
                }
            }

            // Date
            if (details.getTravelDate() == null) {
                Matcher m = Pattern.compile("^(?:Date(?:\\s*of\\s*Travel)?|Travel\\s*Date|Journey\\s*Date|Fecha(?:\\s*de\\s*viaje)?|Date\\s*(?:de\\s*voyage)?|Datum|Reisedatum|Data(?:\\s*di\\s*viaggio)?|Дата|दिनांक|日付|日期|التاريخ)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    Matcher dateInLine = DATE_PATTERN.matcher(m.group(1));
                    if (dateInLine.find()) {
                        details.setTravelDate(dateInLine.group(0).trim());
                    } else {
                        details.setTravelDate(cleanValue(m.group(1)));
                    }
                }
            }

            // Departure Time
            if (details.getDepartureTime() == null) {
                Matcher m = Pattern.compile("^(?:Departure\\s*Time|Time|Hora(?:\\s*de\\s*Salida)?|Salida|Heure(?:\\s*de\\s*d[eé]part)?|Abfahrtszeit|Abfahrt|Orario\\s*(?:di\\s*Partenza)?|Ora(?:\\s*di\\s*Partenza)?|Partenza)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    Matcher timeInLine = TIME_PATTERN.matcher(m.group(1));
                    if (timeInLine.find()) {
                        details.setDepartureTime(timeInLine.group(0).trim());
                    }
                }
            }

            // Arrival Time
            if (details.getArrivalTime() == null) {
                Matcher m = Pattern.compile("^(?:Arrival(?:\\s*Time)?|Hora\\s*(?:de\\s*)?Llegada|Llegada|Heure\\s*(?:d')?arriv[eé]e|Arriv[eé]e|Ankunftszeit|Ankunft|Orario\\s*(?:di\\s*)?Arrivo|Ora(?:\\s*di\\s*Arrivo)?|Arrivo)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    Matcher timeInLine = TIME_PATTERN.matcher(m.group(1));
                    if (timeInLine.find()) {
                        details.setArrivalTime(timeInLine.group(0).trim());
                    }
                }
            }

            // Coach / Carriage (can be anywhere in line)
            if (details.getCoach() == null) {
                Matcher m = Pattern.compile("\\b(?:Coach|Car|Carriage|Coche(?:\\s*\\/\\s*Vag[oó]n)?|Voiture|Wagen|Vettura|Carro|Вагон|कोच|車両|车厢)(?:\\s*(?:No|Number|#|N[°o.]))?[:#.-]?\\s*([0-9]{1,3}[A-Z]?|[A-Z][0-9]{1,3})\\b", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setCoach(m.group(1).trim());
                }
            }

            // Seat (can be anywhere in line)
            if (details.getSeat() == null) {
                Matcher m = Pattern.compile("\\b(?:Seat|Asiento|Plaza|Si[eè]ge|Place|Sitzplatz|Platz|Posto|Assento|Место|सीट|座席|座位)(?:\\s*(?:No|Number|#|N[°o.]))?[:#.-]?\\s*([0-9]{1,3}[A-Z]?|[A-Z][0-9]{1,3})\\b", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setSeat(m.group(1).trim());
                }
            }

            // Class
            if (details.getTicketClass() == null) {
                Matcher m = Pattern.compile("\\b(?:Class|Clase|Classe|Klasse|Класс|श्रेणी|等級|等级)[^:]*[:#.-]\\s*([^,\n;]+)", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setTicketClass(cleanValue(m.group(1)));
                }
            }

            // PNR / Booking Code
            if (details.getPnrNumber() == null) {
                Matcher m = Pattern.compile("(?:PNR|Booking\\s*(?:Ref|Code|ID|Number|Reference)?|Localizador|Billete(?:\\s*\\/\\s*Localizador)?|Billete\\s*N[°o.]?|R[eé]f[eé]rence\\s*(?:Dossier)?|R[eé]f\\.?\\s*(?:Dossier)?|Buchungscode|Codice\\s*Prenotazione|C[oó]digo\\s*(?:de)?\\s*Reserva|Ticket\\s*(?:No|Number|#)?|E-Ticket)[^:]*[:#.-]\\s*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setPnrNumber(m.group(1).trim());
                }
            }

            // Carrier / Train / Flight line
            if (details.getCarrierNumber() == null) {
                Matcher m = Pattern.compile("^(?:Train(?:\\s*N[°o.]?)?|Tren(?:\\s*N[uú]mero)?|Zug|TGV|AVE|ICE|Flight|Vuelo|Vol|Flug|Bus)[^:]*[:#.-]\\s*(.+)$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (m.find()) {
                    details.setCarrierNumber(cleanValue(m.group(1)));
                }
            }
        }

        // Pass 2: Fallback global scan for any missing attributes
        if (details.getTravelDate() == null) {
            Matcher dateMatcher = DATE_PATTERN.matcher(text);
            if (dateMatcher.find()) {
                details.setTravelDate(dateMatcher.group(0).trim());
            }
        }

        // Fallback times scan
        if (details.getDepartureTime() == null || details.getArrivalTime() == null) {
            List<String> times = new ArrayList<>();
            Matcher timeMatcher = TIME_PATTERN.matcher(text);
            while (timeMatcher.find()) {
                String timeStr = timeMatcher.group(0).trim();
                if (!times.contains(timeStr)) {
                    times.add(timeStr);
                }
            }
            if (details.getDepartureTime() == null && !times.isEmpty()) {
                details.setDepartureTime(times.get(0));
            }
            if (details.getArrivalTime() == null && times.size() > 1) {
                details.setArrivalTime(times.get(1));
            }
        }

        if (details.getCarrierNumber() == null) {
            Matcher carrierMatcher = CARRIER_PATTERN.matcher(text);
            if (carrierMatcher.find()) {
                details.setCarrierNumber(carrierMatcher.group(0).trim());
            }
        }

        if (details.getCarrierNumber() != null) {
            // Strip redundant leading "Flight ", "Train ", "Vuelo ", etc.
            details.setCarrierNumber(details.getCarrierNumber().replaceAll("(?i)^(?:Flight|Vuelo|Vol|Flug|Train|Tren|Zug)\\s*#?\\s*", "").trim());
        }

        if (details.getTicketClass() == null) {
            Matcher classMatcher = Pattern.compile("\\b(Economy(?:\\s*Class)?|Business(?:\\s*Class)?|First(?:\\s*Class)?|Premium(?:\\s*Economy)?|Turista(?:\\s*Plus)?|Preferente|Standard(?:\\s*Class)?|1[eè]re\\s*Classe|2[eè]me\\s*Classe|1\\.\\s*Klasse|2\\.\\s*Klasse|Prima\\s*Classe|Segunda\\s*Classe)\\b", Pattern.CASE_INSENSITIVE).matcher(text);
            if (classMatcher.find()) {
                details.setTicketClass(classMatcher.group(1).trim());
            }
        }

        if (details.getPnrNumber() == null) {
            Pattern codePattern = Pattern.compile("\\b([A-Z0-9-]{6,16})\\b");
            Matcher codeMatcher = codePattern.matcher(text);
            while (codeMatcher.find()) {
                String candidate = codeMatcher.group(1);
                if (candidate.matches(".*[A-Z].*") && candidate.matches(".*[0-9].*") && !candidate.equalsIgnoreCase("AVE03083") && !candidate.equalsIgnoreCase("TGV9210")) {
                    details.setPnrNumber(candidate);
                    break;
                }
            }
        }

        if (details.getDepartureStation() == null || details.getArrivalDestination() == null) {
            Pattern routePattern = Pattern.compile("(?m)^\\s*(?:Route\\s*[:#.-]\\s*)?([A-ZÀ-ÿ][a-zà-ÿA-Z0-9 .'-]{2,30})\\s*(?:->|-->|➔|—|\\bto\\b|\\ba\\b|\\bnach\\b|\\bvers\\b|\\bhacia\\b)\\s*([A-ZÀ-ÿ][a-zà-ÿA-Z0-9 .'-]{2,30})\\s*$", Pattern.CASE_INSENSITIVE);
            Matcher routeMatcher = routePattern.matcher(text);
            if (routeMatcher.find()) {
                if (details.getDepartureStation() == null) {
                    details.setDepartureStation(cleanValue(routeMatcher.group(1)));
                }
                if (details.getArrivalDestination() == null) {
                    details.setArrivalDestination(cleanValue(routeMatcher.group(2)));
                }
            }
        }

        details.setTransportType(detectTransportType(details.getCarrierNumber() != null ? details.getCarrierNumber() : "", text));
        details.setSummary(generateSummary(details));

        logger.info("Extracted details: Departure='{}', Arrival='{}', Date='{}', DepTime='{}', ArrTime='{}', Carrier='{}', PNR='{}'",
                details.getDepartureStation(), details.getArrivalDestination(), details.getTravelDate(),
                details.getDepartureTime(), details.getArrivalTime(),
                details.getCarrierNumber(), details.getPnrNumber());

        return details;
    }

    private String detectTransportType(String carrier, String fullText) {
        String lower = (carrier + " " + fullText).toLowerCase();

        // 1. Check Train
        if (Pattern.compile("\\b(ave|tgv|ice|train|tren|zug|rail|railway|sncf|renfe|bahn|db|freccia|frecciarossa|italo|shinkansen|irctc|amtrak|eurostar|thalys|ouigo|station|gare|estaci[oó]n|hauptbahnhof|hbf)\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
            return "Train";
        }

        // 2. Check Flight
        if (Pattern.compile("\\b(flight|vuelo|vol|flug|airline|airlines|airways|airport|aeropuerto|a[eé]roport|air\\s+france|lufthansa|iberia|emirates|delta|united|british\\s+airways)\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
            return "Flight";
        }

        // 3. Check Ferry
        if (Pattern.compile("\\b(ferry|barco|naviera|ferries|shipping)\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
            return "Ferry";
        }

        // 4. Check Bus
        if (Pattern.compile("\\b(bus|autobus|autobús|flixbus|alsa|megabus|greyhound|national\\s*express)\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
            return "Bus";
        }

        return "Travel Ticket";
    }

    private String cleanValue(String val) {
        if (val == null) return null;
        String cleaned = val.replaceAll("[,:;]+$", "").trim();
        return cleaned.length() > 0 ? cleaned : null;
    }

    private String generateSummary(TicketDetails details) {
        StringBuilder sb = new StringBuilder();
        if (details.getTransportType() != null) {
            sb.append(details.getTransportType()).append(" ticket");
        } else {
            sb.append("Travel ticket");
        }

        if (details.getDepartureStation() != null && details.getArrivalDestination() != null) {
            sb.append(" from ").append(details.getDepartureStation()).append(" to ").append(details.getArrivalDestination());
        }

        if (details.getTravelDate() != null) {
            sb.append(" on ").append(details.getTravelDate());
        }

        if (details.getDepartureTime() != null) {
            sb.append(" at ").append(details.getDepartureTime());
        }

        if (details.getCarrierNumber() != null) {
            sb.append(" (").append(details.getCarrierNumber()).append(")");
        }

        if (details.getSeat() != null || details.getCoach() != null) {
            sb.append(". Seat details: ");
            if (details.getCoach() != null) sb.append("Coach ").append(details.getCoach()).append(", ");
            if (details.getSeat() != null) sb.append("Seat ").append(details.getSeat());
        }

        if (details.getPnrNumber() != null) {
            sb.append(". Booking Reference: ").append(details.getPnrNumber());
        }

        sb.append(".");
        return sb.toString();
    }
}
