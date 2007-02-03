/*
 * Pigeon: A pigeon club race result management program.
 * Copyright (C) 2005-2007  Paul Richards
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package pigeon.view;

import java.awt.Component;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.JOptionPane;
import pigeon.model.Distance;
import pigeon.model.Organization;
import pigeon.model.Member;
import pigeon.model.Racepoint;
import pigeon.model.ValidationException;

/**
 * Panel to let the user enter the distances (in the form of a table) for a single racepoint or member.
 */
class DistanceEditor<Subject, Target> extends javax.swing.JPanel {

    private static final long serialVersionUID = 42L;

    private SortedMap<Target, Distance> distances;
    private DistancesTableModel<Target> distancesTableModel;

    public static void editMemberDistances(Component parent, Member member, Organization club) throws UserCancelledException {
        SortedMap<Racepoint, Distance> distances = club.getDistancesForMember(member);
        if (distances.isEmpty()) return;
        DistanceEditor<Member, Racepoint> panel = new DistanceEditor<Member, Racepoint>(member, "Racepoint", distances);
        while (true) {
            Object[] options = {"Ok", "Cancel"};
            int result = JOptionPane.showOptionDialog(parent, panel, "Distances", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (result == 0) {
                for (Map.Entry<Racepoint, Distance> entry: distances.entrySet()) {
                    club.setDistance(member, entry.getKey(), entry.getValue());
                }
                break;
            } else {
                result = JOptionPane.showConfirmDialog(parent, "Return to main menu and discard these changes?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    throw new UserCancelledException();
                }
            }
        }
    }

    public static void editRacepointDistances(Component parent, Racepoint racepoint, Organization club) throws UserCancelledException {
        SortedMap<Member, Distance> distances = club.getDistancesForRacepoint(racepoint);
        if (distances.isEmpty()) return;
        DistanceEditor<Racepoint, Member> panel = new DistanceEditor<Racepoint, Member>(racepoint, "Member", distances);
        while (true) {
            Object[] options = {"Ok", "Cancel"};
            int result = JOptionPane.showOptionDialog(parent, panel, "Distances", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (result == 0) {
                for (Map.Entry<Member, Distance> entry: distances.entrySet()) {
                    club.setDistance(entry.getKey(), racepoint, entry.getValue());
                }
                break;
            } else {
                result = JOptionPane.showConfirmDialog(parent, "Return to main menu and discard these changes?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    throw new UserCancelledException();
                }
            }
        }
    }

    public DistanceEditor(Subject subject, String targetTitle, SortedMap<Target, Distance> distances) {
        this.distances = distances;
        this.distancesTableModel = new DistancesTableModel<Target>(targetTitle, distances, true);
        initComponents();
        distancesPanel.setBorder(new javax.swing.border.TitledBorder("Distances For " + subject));
    }

    private void updateDistancesMap() {

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        distancesPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        distancesTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        distancesPanel.setLayout(new java.awt.BorderLayout());

        distancesPanel.setBorder(new javax.swing.border.TitledBorder("Distances For"));
        distancesTable.setModel(distancesTableModel);
        distancesTable.setRowSelectionAllowed(false);
        scrollPane.setViewportView(distancesTable);

        distancesPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        add(distancesPanel, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel distancesPanel;
    private javax.swing.JTable distancesTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
