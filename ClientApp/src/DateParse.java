import java.util.Scanner;

public class DateParse {

	int getYear(String toParse) {
		int theYear = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter("-");
		if ( scanner.hasNext() ) {
			theYear = Integer.parseInt(scanner.next());
		}
		return theYear;
	}

	int getMonth(String toParse) {
		int theMonth = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter("-");
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) {
			theMonth = Integer.parseInt(scanner.next());
		}
		return theMonth;
	}

	int getDay(String toParse) {
		int theDay = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter("-");
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) {
			toParse = (scanner.next());
		}
		Scanner scannerTwo = new Scanner(toParse);
		scannerTwo.useDelimiter(" ");
		if ( scannerTwo.hasNext() ) {
			theDay = Integer.parseInt(scannerTwo.next());
		}
		return theDay;
	}

	int getHour(String toParse) {
		int theHour = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter(" ");
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) {
			toParse = (scanner.next());
		}
		Scanner scannerTwo = new Scanner(toParse);
		scannerTwo.useDelimiter(":");
		if ( scannerTwo.hasNext() ) {
			theHour = Integer.parseInt(scannerTwo.next());
		}
		return theHour;
	}

	int getMinute(String toParse) {
		int theMinute = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter(":");
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) {
			theMinute = Integer.parseInt(scanner.next());
		}
		return theMinute;
	}

	int getSecond(String toParse) {
		int theSecond = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter(":");
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) {
			theSecond = Integer.parseInt(scanner.next());
		}
		return theSecond;
	}

	int getMillSecond(String toParse) {
		int theMillSecond = 0;
		Scanner scanner = new Scanner(toParse);
		scanner.useDelimiter(":");
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) scanner.next();
		if ( scanner.hasNext() ) {
			theMillSecond = Integer.parseInt(scanner.next());
		}
		return theMillSecond;
	}

	boolean isChatNewer(String lastChatLine, String chatNewLine) {

		boolean foundNewerChatLine = false;

		int theLastYear = getYear(lastChatLine);
		int theLastMonth = getMonth(lastChatLine);
		int theLastDay = getDay(lastChatLine);
		int theLastHour = getHour(lastChatLine);
		int theLastMinute = getMinute(lastChatLine);
		int theLastSecond = getSecond(lastChatLine);
		int theLastMillSecond = getMillSecond(lastChatLine);

		int theNewYear = getYear(chatNewLine);
		int theNewMonth = getMonth(chatNewLine);
		int theNewDay = getDay(chatNewLine);
		int theNewHour = getHour(chatNewLine);
		int theNewMinute = getMinute(chatNewLine);
		int theNewSecond = getSecond(chatNewLine);
		int theNewMillSecond = getMillSecond(chatNewLine);

		// The next set of if else statements will check to see if this line is newer than the one previously displayed
		if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == theLastDay && theNewHour == theLastHour && theNewMinute == theLastMinute && theNewSecond == theLastSecond && theNewMillSecond > theLastMillSecond ) {
			foundNewerChatLine = true; // Must be a new chat line by at least part of a millisecond
		} else {
			if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == theLastDay && theNewHour == theLastHour && theNewMinute == theLastMinute && theNewSecond > theLastSecond ) {
				foundNewerChatLine = true; // Must be a new chat line by at least part of a second
			} else {
				if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == theLastDay && theNewHour == theLastHour && theNewMinute > theLastMinute ) {
					foundNewerChatLine = true; // Must be a new chat line by at least part of a minute
				} else {
					if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == theLastDay && theNewHour > theLastHour ) {
						foundNewerChatLine = true; // Must be a new chat line by at least part of a hour
					} else {
						if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay > theLastDay ) {
							foundNewerChatLine = true; // Must be a new chat line by at least part of a day
						} else {
							if ( theNewYear == theLastYear && theNewMonth > theLastMonth ) {
								foundNewerChatLine = true; // Must be a new chat line by at least part of a month
							} else {
								if ( theNewYear > theLastYear ) {
									foundNewerChatLine = true; // Must be a new chat line chat by at least part of year
								}
							}
						}
					}
				}
			}
		}
		return foundNewerChatLine;
	}

}
