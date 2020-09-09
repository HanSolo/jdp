/*
 * Copyright (c) 2020 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.swing.jdp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import static eu.hansolo.swing.jdp.DisplayMode.DATE_AND_TIME;
import static eu.hansolo.swing.jdp.DisplayMode.DATE_ONLY;


public class DatePickerPopup extends JComponent {
    public static final  boolean                       CALENDAR_WEEK_VISIBLE = true;
    public static final  boolean                       CALENDAR_WEEK_HIDDEN  = false;
    private static final DateTimeFormatter             DTF                   = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final int                           MIN_WIDTH             = 120;
    private static final int                           MIN_HEIGHT            = 64;
    private static final int                           PREFERRED_WIDTH       = 330;
    private static final int                           PREFERRED_HEIGHT      = 160;
    private static final int                           MAX_WIDTH             = 2048;
    private static final int                           MAX_HEIGHT            = 2048;
    private              int                           oldWidth              = PREFERRED_WIDTH;
    private              int                           oldHeight             = PREFERRED_HEIGHT;
    protected            DateTimeFormatter             timeFormatter         = DateTimeFormatter.ofPattern("hh:mm");
    protected            DateTimeFormatter             dateFormatter         = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private              DateTimeFormatter             todaysDateFormatter;
    private              List<DatePickerEventObserver> observers;
    private              Locale                        locale;
    private              boolean                       calendarWeekVisible;
    private              boolean                       todaysDateVisible;
    private              DisplayMode                   displayMode;
    private              Color                         textColor;
    private              Color                         weekendColor;
    private              String                        timeFormat;
    private              String                        dateFormat;
    private              ResourceBundle                resourceBundle;
    private              ZonedDateTime                 selectedDate;
    private              LocalTime                     selectedTime;
    private              ZonedDateTime                 currentDate;
    private              ZoneId                        zoneId;
    private              Font                          currentMonthFont;
    private              Font                          daysOfWeekFont;
    private              Font                          calendarWeekFont;
    private              Font                          todaysDateFont;
    private              Font                          dayFont;
    private              Font                          spinnerFont;
    private              List<JLabel>                  daysOfWeek;
    private              List<JLabel>                  calendarWeeks;
    private              List<JButton>                 days;
    private              String[]                      weekDays;
    private              String[]                      weekDaysLong;
    private              DayOfWeek                     startOfWeek;
    private              WeekFields                    weekFields;
    private              JPanel                        buttonPane;
    private              JPanel                        calendarPane;
    private              JButton                       previousYearButton;
    private              JButton                       previousMonthButton;
    private              JLabel                        currentMonthLabel;
    private              JButton                       nextMonthButton;
    private              JButton                       nextYearButton;
    private              JSpinner                      timeSpinner;
    private              JLabel                        todaysDateLabel;
    private              ActionListener                controlClickListener;
    private              ActionListener                onClickListener;
    private              ChangeListener                timeChangeListener;
    private              boolean                       isDirty;


    // ******************** Constructors **************************************
    public DatePickerPopup() {
        this(Locale.getDefault(), false, true, ZonedDateTime.now(), ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.mm.YYYY", Color.black, Color.red);
    }
    public DatePickerPopup(final LocalDate selectedDate) {
        this(Locale.getDefault(), false, true, ZonedDateTime.of(selectedDate, LocalTime.now(), ZoneId.systemDefault()), ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.mm.YYYY", Color.black, Color.red);
    }
    public DatePickerPopup(final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.mm.YYYY", Color.black, Color.red);
    }
    public DatePickerPopup(final boolean calendarWeekVisible, final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.mm.YYYY", Color.black, Color.red);
    }
    public DatePickerPopup(final Locale locale, final ZonedDateTime selectedDate) {
        this(locale, false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.mm.YYYY", Color.black, Color.red);
    }
    public DatePickerPopup(final Locale locale, final boolean calendarWeekVisible, final boolean todaysDateVisible, final ZonedDateTime selectedDate, final ZoneId zoneId, final DisplayMode displayMode, final String timeFormat, final String dateFormat, final Color textColor, final Color weekEndColor) {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        this.locale               = locale;
        this.zoneId               = zoneId;
        this.todaysDateFormatter  = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
        this.calendarWeekVisible  = calendarWeekVisible;
        this.todaysDateVisible    = todaysDateVisible;
        this.displayMode          = displayMode;
        this.textColor            = textColor;
        this.weekendColor         = weekEndColor;
        this.timeFormat           = timeFormat;
        this.dateFormat           = dateFormat;
        this.resourceBundle       = ResourceBundle.getBundle("eu.hansolo.swing.jdp.DatePickerBundle", this.locale);
        this.observers            = new CopyOnWriteArrayList();
        this.selectedDate         = selectedDate;
        this.selectedTime         = selectedDate.toLocalTime();
        this.currentDate          = selectedDate;
        this.currentMonthFont     = new Font("SansSerif", Font.BOLD, 10);
        this.daysOfWeekFont       = new Font("SansSerif", Font.PLAIN, 10);
        this.calendarWeekFont     = new Font("SansSerif", Font.PLAIN, 8);
        this.dayFont              = new Font("SansSerif", Font.PLAIN, 10);
        this.spinnerFont          = new Font("SansSerif", Font.PLAIN, 12);
        this.daysOfWeek           = new ArrayList<>();
        this.calendarWeeks        = new ArrayList<>();
        this.days                 = new ArrayList<>();
        this.weekFields           = WeekFields.of(this.locale);
        this.startOfWeek          = weekFields.getFirstDayOfWeek();
        this.controlClickListener = e -> {
            Object src = e.getSource();
            if (src.equals(previousYearButton)) {
                setCurrentDate(getCurrentDate().minusYears(1));
            } else if (src.equals(previousMonthButton)) {
                setCurrentDate(getCurrentDate().minusMonths(1));
            } else if (src.equals(nextMonthButton)) {
                setCurrentDate(getCurrentDate().plusMonths(1));
            } else if (src.equals(nextYearButton)) {
                setCurrentDate(getCurrentDate().plusYears(1));
            }
        };
        this.onClickListener      = e -> {
            JButton button = (JButton) e.getSource();
            ZonedDateTime selected = ZonedDateTime.of(LocalDate.of(getCurrentDate().getYear(), getCurrentDate().getMonthValue(), Integer.parseInt(button.getText())), selectedTime, zoneId);
            setSelectedDate(selected);
            button.setSelected(true);
            button.requestFocus();
            fireDatePickerEvent(new DatePickerEvent(DatePickerPopup.this, DatePickerEventType.DATE_SELECTED, selected));
        };
        this.timeChangeListener   = e -> {
            this.selectedTime = LocalTime.ofInstant((((Date) timeSpinner.getValue()).toInstant()), getZoneId());
            ZonedDateTime selected = ZonedDateTime.of(getSelectedDate().toLocalDate(), selectedTime, getZoneId());
            fireDatePickerEvent(new DatePickerEvent(DatePickerPopup.this, DatePickerEventType.DATE_SELECTED, selected));
        };

        init();
        registerListeners();
    }


    // ******************** Private Methods ***********************************
    private void init() {
        setLayout(new GridBagLayout());
        GridBagConstraints gridConstraints = new GridBagConstraints();


        // ******************** Button Pane ***********************************
        buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        GridBagConstraints topGridConstraints = new GridBagConstraints();

        int buttonWidth   = 36;
        int labelWidth    = 96;
        int controlHeight = 20;

        previousYearButton = new JButton("\u25c0\u25c0");
        previousYearButton.setPreferredSize(new Dimension(buttonWidth, controlHeight));
        previousYearButton.setMinimumSize(new Dimension(buttonWidth, controlHeight));
        previousYearButton.setMaximumSize(new Dimension(buttonWidth, controlHeight));
        topGridConstraints.fill      = GridBagConstraints.HORIZONTAL;
        topGridConstraints.gridy     = 0;
        topGridConstraints.gridwidth = 1;
        topGridConstraints.weightx   = 0.1;
        topGridConstraints.insets    = new Insets(0, 0, 0, 0);
        buttonPane.add(previousYearButton, topGridConstraints);

        previousMonthButton = new JButton("\u25c0");
        previousMonthButton.setPreferredSize(new Dimension(buttonWidth, controlHeight));
        previousMonthButton.setMinimumSize(new Dimension(buttonWidth, controlHeight));
        previousMonthButton.setMaximumSize(new Dimension(buttonWidth, controlHeight));
        topGridConstraints.fill      = GridBagConstraints.HORIZONTAL;
        topGridConstraints.gridy     = 0;
        topGridConstraints.gridwidth = 1;
        topGridConstraints.weightx   = 0.1;
        topGridConstraints.insets    = new Insets(0, 5, 0, 5);
        buttonPane.add(previousMonthButton, topGridConstraints);

        currentMonthLabel = new JLabel(DTF.format(getCurrentDate()));
        currentMonthLabel.setPreferredSize(new Dimension(labelWidth, controlHeight));
        currentMonthLabel.setMinimumSize(new Dimension(labelWidth, controlHeight));
        currentMonthLabel.setMaximumSize(new Dimension(labelWidth, controlHeight));
        currentMonthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topGridConstraints.fill      = GridBagConstraints.HORIZONTAL;
        topGridConstraints.gridy     = 0;
        topGridConstraints.weightx   = 0.6;
        topGridConstraints.gridwidth = isCalendarWeekVisible() ? 4 : 3;
        topGridConstraints.insets    = new Insets(0, 5, 0, 5);
        buttonPane.add(currentMonthLabel, topGridConstraints);

        nextMonthButton = new JButton("\u25ba");
        nextMonthButton.setPreferredSize(new Dimension(buttonWidth, controlHeight));
        nextMonthButton.setMinimumSize(new Dimension(buttonWidth, controlHeight));
        nextMonthButton.setMaximumSize(new Dimension(buttonWidth, controlHeight));
        topGridConstraints.fill      = GridBagConstraints.HORIZONTAL;
        topGridConstraints.gridy     = 0;
        topGridConstraints.gridwidth = 1;
        topGridConstraints.weightx   = 0.1;
        topGridConstraints.insets    = new Insets(0, 5, 0, 5);
        buttonPane.add(nextMonthButton, topGridConstraints);

        nextYearButton = new JButton("\u25ba\u25ba");
        nextYearButton.setPreferredSize(new Dimension(buttonWidth, controlHeight));
        nextYearButton.setMinimumSize(new Dimension(buttonWidth, controlHeight));
        nextYearButton.setMaximumSize(new Dimension(buttonWidth, controlHeight));
        topGridConstraints.fill      = GridBagConstraints.HORIZONTAL;
        topGridConstraints.gridy     = 0;
        topGridConstraints.gridwidth = 1;
        topGridConstraints.weightx   = 0.1;
        topGridConstraints.insets    = new Insets(0, 0, 0, 0);
        buttonPane.add(nextYearButton, topGridConstraints);


        gridConstraints.fill      = GridBagConstraints.VERTICAL;
        gridConstraints.gridx     = 0;
        gridConstraints.gridy     = 0;
        gridConstraints.weightx   = 1;
        gridConstraints.weighty   = 0.25;
        //gridConstraints.insets    = new Insets(0, 0, 2, 0);
        add(buttonPane, gridConstraints);


        // ******************** Calendar Pane *********************************
        if (DATE_ONLY == getDisplayMode() || DATE_AND_TIME == getDisplayMode()) {
            int noOfColumns = isCalendarWeekVisible() ? 8 : 7;
            int noOfRows    = 7;
            calendarPane = new JPanel();
            calendarPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            calendarPane.setLayout(new GridLayout(noOfRows, noOfColumns, 1, 1));
            //calendarPane.setLayout(new GridLayout(noOfRows, noOfColumns));

            if (isCalendarWeekVisible()) {
                JLabel label = new JLabel("");
                calendarPane.add(label);
            }

            weekDays = getWeekDays();
            weekDaysLong = getWeekDaysLong();

            // Days of week
            for (int i = 0; i < weekDays.length; i++) {
                JLabel dayLabel = new JLabel(weekDays[i]);
                dayLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
                dayLabel.setFont(daysOfWeekFont);
                if (DayOfWeek.MONDAY == startOfWeek) {
                    if (i == 5 || i == 6) {
                        dayLabel.setForeground(getWeekendColor());
                    } else {
                        dayLabel.setForeground(getTextColor());
                    }
                } else {
                    if (i == 0 || i == 6) {
                        dayLabel.setForeground(getWeekendColor());
                    } else {
                        dayLabel.setForeground(getTextColor());
                    }
                }
                calendarPane.add(dayLabel);
                daysOfWeek.add(dayLabel);
            }

            int        day            = 1;
            int        month          = currentDate.getMonthValue();
            int        year           = currentDate.getYear();
            LocalDate  currentDate    = LocalDate.of(year, month, day);
            WeekFields weekFields     = WeekFields.of(this.locale);
            int        firstDayOffset = currentDate.getDayOfWeek().getValue() - (DayOfWeek.MONDAY == startOfWeek ? 1 : 0);
            int        firstWeekNo    = currentDate.get(weekFields.weekOfWeekBasedYear());

            JLabel calendarWeek = new JLabel(String.format("%01d", firstWeekNo));
            calendarWeek.setHorizontalTextPosition(SwingConstants.CENTER);
            calendarWeek.setHorizontalAlignment(SwingConstants.CENTER);
            calendarWeek.setFont(calendarWeekFont);
            calendarWeek.setForeground(getTextColor());
            if (isCalendarWeekVisible()) {
                calendarPane.add(calendarWeek);
            }
            calendarWeeks.add(calendarWeek);

            // First row
            int dayOffset = isCalendarWeekVisible() ? 1 : 0;
            for (int i = dayOffset; i < noOfColumns; i++) {
                if (i < firstDayOffset + dayOffset) {
                    JLabel offsetLabel = new JLabel("");
                    calendarPane.add(offsetLabel);
                } else {
                    try {
                        JButton button = new JButton(Integer.toString(day));
                        button.setHorizontalTextPosition(SwingConstants.CENTER);
                        button.setFont(dayFont);
                        button.setForeground(getTextColor());
                        if (day == getCurrentDate().getDayOfMonth()) {
                            button.setSelected(true);
                            button.requestFocus();
                        }
                        calendarPane.add(button);
                        days.add(button);
                        day++;
                    } catch (DateTimeException e) {
                        continue;
                    }
                }
            }

            // All other rows
            int startRow = firstDayOffset == 7 ? 2 : 2;
            for (int r = startRow; r < noOfRows; r++) {
                for (int c = 0; c < noOfColumns; c++) {
                    try {
                        currentDate = LocalDate.of(year, month, day);
                        if (isCalendarWeekVisible() && c == 0) {
                            weekFields = WeekFields.of(locale);
                            calendarWeek = new JLabel(String.format("%01d", currentDate.get(weekFields.weekOfWeekBasedYear())));
                            calendarWeek.setHorizontalTextPosition(SwingConstants.CENTER);
                            calendarWeek.setHorizontalAlignment(SwingConstants.CENTER);
                            calendarWeek.setFont(calendarWeekFont);
                            calendarWeek.setForeground(getTextColor());
                            calendarPane.add(calendarWeek);
                            calendarWeeks.add(calendarWeek);
                        } else {
                            JButton button = new JButton(Integer.toString(day));
                            button.setHorizontalTextPosition(SwingConstants.CENTER);
                            button.setFont(dayFont);
                            button.setForeground(getTextColor());
                            if (day == getCurrentDate().getDayOfMonth()) {
                                button.setSelected(true);
                                button.requestFocus();
                            }
                            calendarPane.add(button);
                            days.add(button);
                            day++;
                        }
                    } catch (DateTimeException e) {
                        JLabel offsetLabel = new JLabel("");
                        calendarPane.add(offsetLabel);
                        continue;
                    }
                }
            }

            gridConstraints.fill = GridBagConstraints.VERTICAL;
            gridConstraints.gridx = 0;
            gridConstraints.gridy = 1;
            gridConstraints.gridwidth = isCalendarWeekVisible() ? 8 : 7;
            gridConstraints.weightx = 1;
            gridConstraints.weighty = 0.75;
            add(calendarPane, gridConstraints);
        }


        // ******************** Time Pane *********************************
        if (DisplayMode.TIME_ONLY == getDisplayMode() || DATE_AND_TIME == getDisplayMode()) {
            Date             date      = Date.from(selectedTime.atDate(getSelectedDate().toLocalDate()).atZone(getZoneId()).toInstant());
            Date             startTime = Date.from((ZonedDateTime.of(getSelectedDate().toLocalDate(), LocalTime.MIN, getZoneId())).toInstant());
            Date             endTime   = Date.from((ZonedDateTime.of(getSelectedDate().toLocalDate(), LocalTime.MAX, getZoneId())).toInstant());
            SpinnerDateModel model     = new SpinnerDateModel(startTime, null, endTime, Calendar.MINUTE);

            timeSpinner = new JSpinner();
            timeSpinner.setModel(model);
            timeSpinner.setValue(date);
            timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, getTimeFormat()));
            timeSpinner.addChangeListener(timeChangeListener);

            gridConstraints.fill      = GridBagConstraints.VERTICAL;
            gridConstraints.gridx     = 0;
            gridConstraints.gridy     = 2;
            gridConstraints.gridwidth = isCalendarWeekVisible() ? 8 : 7;
            gridConstraints.weightx   = 1;
            gridConstraints.weighty   = 0.75;
            add(timeSpinner, gridConstraints);
        }


        // ******************** Todays date Pane **************************
        if (isTodaysDateVisible()) {
            ZonedDateTime now = ZonedDateTime.now(getZoneId());
            todaysDateLabel = new JLabel(resourceBundle.getString("todays_date") + now.format(todaysDateFormatter));
            todaysDateLabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, controlHeight));
            todaysDateLabel.setMinimumSize(new Dimension(Integer.MAX_VALUE, controlHeight));
            todaysDateLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, controlHeight));
            todaysDateLabel.setHorizontalAlignment(SwingConstants.CENTER);

            gridConstraints.fill      = GridBagConstraints.VERTICAL;
            gridConstraints.gridx     = 0;
            gridConstraints.gridy     = 3;
            gridConstraints.gridwidth = isCalendarWeekVisible() ? 8 : 7;
            gridConstraints.weightx   = 1;
            gridConstraints.weighty   = 0.75;
            add(todaysDateLabel, gridConstraints);
        }
    }

    private void reInit() {
        removeAll();

        previousYearButton.removeActionListener(controlClickListener);
        previousMonthButton.removeActionListener(controlClickListener);
        nextMonthButton.removeActionListener(controlClickListener);
        nextYearButton.removeActionListener(controlClickListener);

        timeSpinner.removeChangeListener(timeChangeListener);

        days.forEach(button -> button.removeActionListener(onClickListener));
        daysOfWeek.clear();
        calendarWeeks.clear();
        days.clear();

        init();
        registerListeners();

        paintComponent(getGraphics());
    }

    private void registerListeners() {
        previousYearButton.addActionListener(controlClickListener);
        previousMonthButton.addActionListener(controlClickListener);
        nextMonthButton.addActionListener(controlClickListener);
        nextYearButton.addActionListener(controlClickListener);
        for (JButton button : days) {
            button.addActionListener(onClickListener);
        }
    }

    private String[] getWeekDays() {
        final String[] weekDays;
        if (DayOfWeek.MONDAY == startOfWeek) {
            weekDays = new String[] {
                resourceBundle.getString("mon"),
                resourceBundle.getString("tue"),
                resourceBundle.getString("wed"),
                resourceBundle.getString("thu"),
                resourceBundle.getString("fri"),
                resourceBundle.getString("sat"),
                resourceBundle.getString("sun")
            };
        } else {
            weekDays = new String[] {
                resourceBundle.getString("sun"),
                resourceBundle.getString("mon"),
                resourceBundle.getString("tue"),
                resourceBundle.getString("wed"),
                resourceBundle.getString("thu"),
                resourceBundle.getString("fri"),
                resourceBundle.getString("sat")
            };
        }
        return weekDays;
    }

    private String[] getWeekDaysLong() {
        final String[] weekDaysLong;
        if (DayOfWeek.MONDAY == startOfWeek) {
            weekDaysLong = new String[] {
                resourceBundle.getString("mon_long"),
                resourceBundle.getString("tue_long"),
                resourceBundle.getString("wed_long"),
                resourceBundle.getString("thu_long"),
                resourceBundle.getString("fri_long"),
                resourceBundle.getString("sat_long"),
                resourceBundle.getString("sun_long")
            };
        } else {
            weekDaysLong = new String[] {
                resourceBundle.getString("sun_long"),
                resourceBundle.getString("mon_long"),
                resourceBundle.getString("tue_long"),
                resourceBundle.getString("wed_long"),
                resourceBundle.getString("thu_long"),
                resourceBundle.getString("fri_long"),
                resourceBundle.getString("sat_long")
            };
        }
        return weekDaysLong;
    }

    private int clamp(final int min, final int max, final int value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }


    // ******************** Public Methods ************************************
    @Override public Dimension getMinimumSize() {
        return new Dimension(MIN_WIDTH, MIN_HEIGHT);
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }
    @Override public Dimension getMaximumSize() {
        return new Dimension(MAX_WIDTH, MAX_HEIGHT);
    }

    public ZonedDateTime getSelectedDate() {
        return selectedDate;
    }
    public void setSelectedDate(final ZonedDateTime selectedDate) {
        this.selectedDate = selectedDate;
        isDirty = true;
        reInit();
    }

    public ZonedDateTime getCurrentDate() {
        return currentDate;
    }
    public void setCurrentDate(final ZonedDateTime currentDate) {
        this.currentDate = currentDate;
        isDirty = true;
        reInit();
    }

    public Locale getLocale() {
        return this.locale;
    }
    public void setLocale(final Locale locale) {
        this.locale              = locale;
        this.weekFields          = WeekFields.of(this.locale);
        this.startOfWeek         = weekFields.getFirstDayOfWeek();
        this.resourceBundle      = ResourceBundle.getBundle("eu.hansolo.swing.jdp.DatePickerBundle", this.locale);
        this.todaysDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
        reInit();
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
    public void setZoneId(final ZoneId zoneId) {
        this.zoneId = zoneId;
        reInit();
    }

    public boolean isCalendarWeekVisible() {
        return calendarWeekVisible;
    }
    public void setCalendarWeekVisible(final boolean calendarWeekVisible) {
        this.calendarWeekVisible = calendarWeekVisible;
        reInit();
    }

    public boolean isTodaysDateVisible() {
        return todaysDateVisible;
    }
    public void setTodaysDateVisible(final boolean todaysDateVisible) {
        this.todaysDateVisible = todaysDateVisible;
        reInit();
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }
    public void setDisplayMode(final DisplayMode displayMode) {
        this.displayMode = displayMode;
        reInit();
    }

    public Color getTextColor() {
        return textColor;
    }
    public void setTextColor(final Color textColor) {
        this.textColor = textColor;
        reInit();
    }

    public Color getWeekendColor() {
        return weekendColor;
    }
    public void setWeekendColor(final Color weekendColor) {
        this.weekendColor = weekendColor;
        reInit();
    }

    public String getTimeFormat() {
        return timeFormat;
    }
    public void setTimeFormat(final String timeFormat) {
        this.timeFormat    = timeFormat;
        this.timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
        paintComponent(getGraphics());
    }

    public String getDateFormat() {
        return dateFormat;
    }
    public void setDateFormat(final String dateFormat) {
        this.dateFormat    = dateFormat;
        this.dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        paintComponent(getGraphics());
    }

    public void setOnDatePickerEvent(final DatePickerEventObserver observer) {
        addDatePickerEventObserver(observer);
    }
    public void addDatePickerEventObserver(final DatePickerEventObserver observer) {
        if (observers.contains(observer)) { return; }
        observers.add(observer);
    }
    public void removeDatePickerEventObserver(final DatePickerEventObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    public void fireDatePickerEvent(final DatePickerEvent event) {
        observers.forEach(observer -> observer.onDatePickerEvent(event));
    }

    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getWidth() != oldWidth || getHeight() != oldHeight || isDirty) {
            final int minHeight = getHeight() / 10;
            final int cellWidth = getWidth() / 28;
            final int fontSize  = clamp(5, minHeight, cellWidth);

            this.currentMonthFont = new Font("SansSerif", Font.BOLD, clamp(5, 16, fontSize + 2));
            this.daysOfWeekFont   = new Font("SansSerif", Font.PLAIN, fontSize);
            this.calendarWeekFont = new Font("SansSerif", Font.PLAIN, fontSize - 2);
            this.dayFont          = new Font("SansSerif", Font.PLAIN, fontSize);
            this.spinnerFont      = new Font("SansSerif", Font.PLAIN, fontSize + 2);

            this.previousYearButton.setFont(currentMonthFont);
            this.previousMonthButton.setFont(currentMonthFont);
            this.currentMonthLabel.setFont(currentMonthFont);
            this.nextMonthButton.setFont(currentMonthFont);
            this.nextYearButton.setFont(currentMonthFont);

            this.timeSpinner.setFont(spinnerFont);

            for (JLabel label : daysOfWeek) {
                label.setFont(daysOfWeekFont);
            }
            for (JLabel label : calendarWeeks) {
                label.setFont(calendarWeekFont);
            }
            for (JButton button : days) {
                button.setFont(dayFont);
                button.setSelected(false);
                if (Integer.parseInt(button.getText()) == getSelectedDate().getDayOfMonth()) {
                    if (DTF.format(getSelectedDate()).equals(currentMonthLabel.getText())) {
                        button.setSelected(true);
                        button.requestFocus();
                    }
                }
            }

            if (cellWidth > minHeight * 0.2) {
                for (int i = 0; i < weekDaysLong.length; i++) {
                    daysOfWeek.get(i).setFont(daysOfWeekFont);
                    daysOfWeek.get(i).setText(weekDaysLong[i]);
                }
            } else {
                for (int i = 0; i < weekDays.length; i++) {
                    daysOfWeek.get(i).setFont(daysOfWeekFont);
                    daysOfWeek.get(i).setText(weekDays[i]);
                }
            }

            if (isTodaysDateVisible()) {
                todaysDateFont = new Font("SansSerif", Font.BOLD, fontSize);
                todaysDateLabel.setFont(todaysDateFont);
            }

            isDirty = false;
        }

        oldWidth  = getWidth();
        oldHeight = getHeight();
    }
}
