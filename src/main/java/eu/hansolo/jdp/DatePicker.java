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
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
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
    public static final  boolean             CALENDAR_WEEK_VISIBLE = true;
    public static final  boolean             CALENDAR_WEEK_HIDDEN  = false;
    public static final  boolean             TODAYS_DATE_VISIBLE   = true;
    public static final  boolean             TODAYS_DATE_HIDDEN    = false;
    private              JFormattedTextField dateField;
    private              DatePickerPopup     popup;
    private              JDialog             dialog;
    private              boolean             autoClosePopup;

    // ******************** Constructors **************************************
    public DatePicker() {
        this(Locale.getDefault(), false, true, ZonedDateTime.now(), ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red, true);
    }
    public DatePicker(final LocalDate selectedDate) {

        this(Locale.getDefault(), false, true, ZonedDateTime.of(selectedDate, LocalTime.now(), ZoneId.systemDefault()), ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red, true);
    }
    public DatePicker(final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red, true);
    }
    public DatePicker(final boolean calendarWeekVisible, final ZonedDateTime selectedDate) {
        this(Locale.getDefault(), calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red, true);
    }
    public DatePicker(final Locale locale, final ZonedDateTime selectedDate) {
        this(locale, false, true, selectedDate, ZoneId.systemDefault(), DATE_AND_TIME, Color.black, Color.red, true);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final ZonedDateTime selectedDate, final DisplayMode displayMode) {
        this(locale, calendarWeekVisible, true, selectedDate, ZoneId.systemDefault(), displayMode, Color.black, Color.red, true);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final boolean todaysDateVisible, final ZonedDateTime selectedDate, final ZoneId zoneId, final DisplayMode displayMode, final Color textColor, final Color weekendColor) {
        this(locale, calendarWeekVisible, todaysDateVisible, selectedDate, zoneId, displayMode, textColor, weekendColor, true);
    }
    public DatePicker(final Locale locale, final boolean calendarWeekVisible, final boolean todaysDateVisible, final ZonedDateTime selectedDate, final ZoneId zoneId, final DisplayMode displayMode, final Color textColor, final Color weekendColor, final boolean autoClosePopup) {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(200, 20));
        setMaximumSize(new Dimension(200, 20));

        this.autoClosePopup = autoClosePopup;

        popup = new DatePickerPopup(locale, calendarWeekVisible, todaysDateVisible, selectedDate, zoneId, displayMode, textColor, weekendColor);

        SimpleDateFormat datePattern = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.DEFAULT, locale);

        dateField = new JFormattedTextField(new SimpleDateFormat(datePattern.toLocalizedPattern()));
        dateField.setHorizontalAlignment(SwingConstants.RIGHT);

        final int fontSize;
        final int buttonWidth;
        final int buttonHeight;
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof MetalLookAndFeel) {
            fontSize     = 12;
            buttonWidth  = 20;
            buttonHeight = 20;
            dateField.setPreferredSize(new Dimension(120, 20));
        } else if (laf instanceof NimbusLookAndFeel) {
            fontSize     = 10;
            buttonWidth  = 36;
            buttonHeight = 24;
            dateField.setPreferredSize(new Dimension(120, 24));
        } else {
            fontSize     = 12;
            buttonWidth  = 20;
            buttonHeight = 20;
            dateField.setPreferredSize(new Dimension(120, 24));
        }

        JButton popupButton = new JButton("\u25c2");
        popupButton.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        popupButton.setVerticalTextPosition(SwingConstants.CENTER);
        popupButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        popupButton.setMargin(new Insets(0, 0, 0, 0));
        popupButton.addActionListener(e -> {
            Point p = dateField.getLocationOnScreen();

            dialog = new JDialog();
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
            switch (popup.getDisplayMode()) {
                case DATE_AND_TIME:
                    dialog.setPreferredSize(new Dimension(280, 310));
                    break;
                case DATE_ONLY:
                    dialog.setPreferredSize(new Dimension(280, 220));
                    break;
                case TIME_ONLY:
                    dialog.setPreferredSize(new Dimension(280, 20));
                    break;
            }
            //dialog.setModal(true);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().add(popup);
            dialog.pack();
            dialog.setLocation(p.x, p.y + dateField.getHeight());
            dialog.setVisible(true);
        });

        popup.setOnDatePickerEvent(e -> {
            dateField.setText(popup.dateFormatter.format(popup.getSelectedDate()));
            if (autoClosePopup) {
                dialog.setVisible(false);
                popupButton.setText("\u25c2");
            }
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

    public boolean isTodaysDateVisible() {
        return popup.isTodaysDateVisible();
    }
    public void setTodaysDateVisible(final boolean visible) {
        popup.setTodaysDateVisible(visible);
    }

    public DisplayMode getDisplayMode() {
        return popup.getDisplayMode();
    }
    public void setDisplayMode(final DisplayMode displayMode) {
        popup.setDisplayMode(displayMode);
    }

    public Color getTextColor() {
        return popup.getTextColor();
    }
    public void setTextColor(final Color textColor) {
        popup.setTextColor(textColor);
    }

    public Color getWeekendColor() {
        return popup.getWeekendColor();
    }
    public void setWeekendColor(final Color weekendColor) {
        popup.setWeekendColor(weekendColor);
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
