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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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


public class DatePicker extends JPanel {
    private DatePickerPopup popup;

    // ******************** Constructors **************************************
    public DatePicker() {
        this(Locale.getDefault(), false, true, ZonedDateTime.now(), ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.MM.yyyy", Color.black, Color.red);
    }
    public DatePicker(final LocalDate selectedDate) {

        this(Locale.getDefault(), false, true, ZonedDateTime.of(selectedDate, LocalTime.now(), ZoneId.systemDefault()), ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.MM.yyyy", Color.black, Color.red);
    }
    public DatePicker(final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.MM.yyyy", Color.black, Color.red);
    }
    public DatePicker(final boolean calendarWeekVisible, final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.MM.yyyy", Color.black, Color.red);
    }
    public DatePicker(final Locale locale, final ZonedDateTime selectedDate) {
        this(locale, false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, "hh:mm", "dd.MM.yyyy", Color.black, Color.red);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final ZonedDateTime selectedDate, final DisplayMode displayMode) {
        this(locale, calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), displayMode, "hh:mm", "dd.MM.yyyy", Color.black, Color.red);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final boolean todaysDateVisible, final ZonedDateTime selectedDate, final ZoneId zoneId, final DisplayMode displayMode, final String timeFormat, final String dateFormat, final Color textColor, final Color weekendColor) {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(200, 20));
        setMaximumSize(new Dimension(200, 20));

        popup = new DatePickerPopup(locale, calendarWeekVisible, todaysDateVisible, selectedDate, zoneId, displayMode, timeFormat, dateFormat, textColor, weekendColor);
        popup.setDateFormat(dateFormat);

        JTextField dateField = new JFormattedTextField(new SimpleDateFormat(dateFormat));
        dateField.setHorizontalAlignment(SwingConstants.RIGHT);
        dateField.setPreferredSize(new Dimension(120, 20));

        popup.setOnDatePickerEvent(e -> dateField.setText(popup.dateFormatter.format(popup.getSelectedDate())));

        JButton popupButton = new JButton("\u25c2");
        popupButton.setPreferredSize(new Dimension(20, 20));
        popupButton.addActionListener(e -> {
            Point p = dateField.getLocationOnScreen();

            JDialog dialog = new JDialog();
            dialog.addWindowFocusListener(new WindowFocusListener() {
                @Override public void windowGainedFocus(final WindowEvent e) {
                    popupButton.setText("\u25be");
                    try {
                        popup.setSelectedDate(ZonedDateTime.parse(dateField.getText()));
                    } catch (DateTimeParseException ex) {

                    }
                }
                @Override public void windowLostFocus(final WindowEvent e) {
                    if (SwingUtilities.isDescendingFrom(e.getOppositeWindow(), dialog)) {
                        return;
                    }
                    popupButton.setText("\u25c2");
                    dialog.setVisible(false);
                }
            });
            dialog.setUndecorated(true);
            dialog.setPreferredSize(new Dimension(280, 310));
            //dialog.setModal(true);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().add(popup);
            dialog.pack();
            dialog.setLocation(p.x, p.y + dateField.getHeight());
            dialog.setVisible(true);
        });

        add(dateField);
        add(popupButton);
    }


    // ******************** Public methods ************************************
    public ZonedDateTime getSelectedDate() {
        return popup.getSelectedDate();
    }
    public void setSelectedDate(final ZonedDateTime selectedDate) {
        popup.setSelectedDate(selectedDate);
    }

    public ZonedDateTime getCurrentDate() {
        return popup.getCurrentDate();
    }
    public void setCurrentDate(final ZonedDateTime currentDate) {
        popup.setCurrentDate(currentDate);
    }


    public void setOnDatePickerEvent(final DatePickerEventObserver observer) {
        addDatePickerEventObserver(observer);
    }
    public void addDatePickerEventObserver(final DatePickerEventObserver observer) {
        popup.addDatePickerEventObserver(observer);
    }
    public void removeDatePickerEventObserver(final DatePickerEventObserver observer) {
        popup.removeDatePickerEventObserver(observer);
    }
}
