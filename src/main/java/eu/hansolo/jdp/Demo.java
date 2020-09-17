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
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.multi.MultiLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import static eu.hansolo.jdp.DatePicker.CALENDAR_WEEK_HIDDEN;
import static eu.hansolo.jdp.DatePicker.TODAYS_DATE_HIDDEN;


public class Demo {

    public Demo() {
        JFrame frame = new JFrame("JDP Java Date Picker");
        frame.setSize(280, 100);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            //UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        */

        //DatePicker datePicker = new DatePicker();
        //datePicker.setLocale(Locale.US);
        //datePicker.setLocale(Locale.GERMANY);
        //datePicker.setTodaysDateVisible(false);
        //datePicker.setDisplayMode(DisplayMode.DATE_ONLY);
        //datePicker.setOnDatePickerEvent(e -> System.out.println("Selected date: " + e.getDate()));

        DatePicker datePicker = new DatePicker(Locale.GERMANY, CALENDAR_WEEK_HIDDEN, TODAYS_DATE_HIDDEN, ZonedDateTime.now(), ZoneId.systemDefault(), DisplayMode.DATE_ONLY, Color.black, Color.black, true);

        frame.getContentPane().add(datePicker);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Demo();
    }
}
