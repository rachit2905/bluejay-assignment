import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EmployeeAnalyzer {
    public static void main(String[] args) {
        String csvFilePath = "Assignment_Timecard.xlsx - Sheet1.csv"; // Replace with your CSV file path

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean skipHeader = true;
            int co = 1; // Flag to skip the first row (header)

            Map<String, Integer> consecutiveDaysMap = new HashMap<>();
            Map<String, Date> lastShiftEndTimeMap = new HashMap<>();

            while ((line = br.readLine()) != null) {
                if (skipHeader && co >= 2) {
                    skipHeader = false;
                    continue; // Skip the header row
                } else if (skipHeader == true) {
                    co++;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length >= 9) {
                    String positionID = data[0];
                    String positionStatus = data[1];
                    String timeIn = data[2];
                    String timeOut = data[3];
                    String timecardHours = data[4];
                    String payCycleStartDate = data[5];
                    String payCycleEndDate = data[6];
                    String employeeName = data[7];
                    String fileNumber = data[8];

                    // Check conditions for hours worked
                    if (isValidTimeFormat(timecardHours)) {
                        int totalMinutes = convertTimeToMinutes(timecardHours);

                        if (totalMinutes > 840) {
                            System.out.println("Employee: " + employeeName + ", Position: " + positionStatus +
                                    " has worked for more than 14 hours in a single shift.");
                        }
                    }

                    // Check consecutive days worked
                    if (consecutiveDaysMap.containsKey(employeeName)) {
                        consecutiveDaysMap.put(employeeName, consecutiveDaysMap.get(employeeName) +
                                1);
                    } else {
                        consecutiveDaysMap.put(employeeName, 1);
                    }

                    // // Check time between shifts
                    if (lastShiftEndTimeMap.containsKey(employeeName)) {
                        Date lastShiftEndTime = lastShiftEndTimeMap.get(employeeName);

                        Date currentTimeIn = parseTime(timeIn);
                        if (currentTimeIn != null) {
                            long timeBetweenShiftsMinutes = (currentTimeIn.getTime() - lastShiftEndTime.getTime())
                                    / (60 * 1000);
                            if (timeBetweenShiftsMinutes > 60 && timeBetweenShiftsMinutes < 600) {
                                System.out.println("Employee: " + employeeName + ", Position: " + positionStatus +
                                        " has less than 10 hours but greater than 1 hour between shifts.");
                            }
                        }
                    }
                    if (parseTime(timeOut) != null)
                        lastShiftEndTimeMap.put(employeeName, parseTime(timeOut));
                }
            }

            // Check for employees who worked for 7 consecutive days
            for (Map.Entry<String, Integer> entry : consecutiveDaysMap.entrySet()) {
                if (entry.getValue() >= 7) {
                    System.out.println("Employee: " + entry.getKey() + " has worked for 7 consecutive days.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidTimeFormat(String time) {
        return time.matches("\\d+:\\d+");
    }

    private static int convertTimeToMinutes(String time) {
        String[] timeParts = time.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        return (hours * 60) + minutes;
    }

    private static Date parseTime(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        try {
            return dateFormat.parse(time);
        } catch (ParseException e) {
            // e.printStackTrace();
            return null;
        }
    }
}
