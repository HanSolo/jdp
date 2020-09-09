/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.jdp;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import static eu.hansolo.jdp.DisplayMode.DATE_AND_TIME;


public class DatePicker extends JPanel {
    private JFormattedTextField dateField;
    private DatePickerPopup     popup;

    // ******************** Constructors **************************************
    public DatePicker() {
        this(Locale.getDefault(), false, true, ZonedDateTime.now(), ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red);
    }
    public DatePicker(final LocalDate selectedDate) {

        this(Locale.getDefault(), false, true, ZonedDateTime.of(selectedDate, LocalTime.now(), ZoneId.systemDefault()), ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red);
    }
    public DatePicker(final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red);
    }
    public DatePicker(final boolean calendarWeekVisible, final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red);
    }
    public DatePicker(final Locale locale, final ZonedDateTime selectedDate) {
        this(locale, false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final ZonedDateTime selectedDate, final DisplayMode displayMode) {
        this(locale, calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), displayMode, Color.black, Color.red);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final boolean todaysDateVisible, final ZonedDateTime selectedDate, final ZoneId zoneId, final DisplayMode displayMode, final Color textColor, final Color weekendColor) {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(200, 20));
        setMaximumSize(new Dimension(200, 20));

        popup = new DatePickerPopup(locale, calendarWeekVisible, todaysDateVisible, selectedDate, zoneId, displayMode, textColor, weekendColor);

        SimpleDateFormat datePattern = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.DEFAULT, locale);

        dateField = new JFormattedTextField(new SimpleDateFormat(datePattern.toLocalizedPattern()));
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
                    try {
                        if (SwingUtilities.isDescendingFrom(e.getOppositeWindow(), dialog)) {
                            return;
                        }
                    } catch (NullPointerException ex) {
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

    public void setLocale(final Locale locale) {
        popup.setLocale(locale);
        dateField.setLocale(locale);
        DateFormatter dateFormatter = (DateFormatter) dateField.getFormatter();
        dateFormatter.setFormat(DateFormat.getDateInstance(DateFormat.DEFAULT, locale));
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
