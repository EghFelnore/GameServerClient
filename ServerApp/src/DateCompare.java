import java.util.Scanner;

public class DateCompare {

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

	boolean isChatRecent(String chatLine, String nowTime) {

		boolean foundRecentChatLine = false;

		int minutesBack = 5;

		int theLastYear = getYear(chatLine);
		int theLastMonth = getMonth(chatLine);
		int theLastDay = getDay(chatLine);
		int theLastHour = getHour(chatLine);
		int theLastMinute = getMinute(chatLine);

		int theNewYear = getYear(nowTime);
		int theNewMonth = getMonth(nowTime);
		int theNewDay = getDay(nowTime);
		int theNewHour = getHour(nowTime);
		int theNewMinute = getMinute(nowTime);

		// The next set of if else statements will check to see if this line is newer than the one previously displayed
		if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == theLastDay && theNewHour == theLastHour && (theNewMinute-minutesBack) <= theLastMinute ) {
			foundRecentChatLine = true; // Must be a new chat line by at least part of a minute
		} else {
			if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == theLastDay && theNewHour == (theLastHour+1) && theNewMinute <= minutesBack ) {
				foundRecentChatLine = true; // Must be a new chat line by at least part of a hour
			} else {
				if ( theNewYear == theLastYear && theNewMonth == theLastMonth && theNewDay == (theLastDay+1) && theNewHour == 0 && theNewMinute <= minutesBack ) {
					foundRecentChatLine = true; // Must be a new chat line by at least part of a day
				} else {
					if ( theNewYear == theLastYear && theNewMonth == (theLastMonth+1) && theNewDay == 1 && theNewHour == 0 && theNewMinute <= minutesBack ) {
						foundRecentChatLine = true; // Must be a new chat line by at least part of a month
					} else {
						if ( theNewYear == (theLastYear+1) && theNewMonth == 1 && theNewDay == 1 && theNewHour == 0 && theNewMinute <= minutesBack ) {
							foundRecentChatLine = true; // Must be a new chat line chat by at least part of year
						}
					}
				}
			}
		}
		return foundRecentChatLine;
	}

}
