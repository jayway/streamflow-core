/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

public class PointSelectionListener implements MouseListener {

   private JXMapViewer mapViewer;
   private GeoMarkerHolder geoMarkerHolder;

   public PointSelectionListener(GeoMarkerHolder geoMarkerHolder,
         JXMapViewer mapViewer) {
      this.geoMarkerHolder = geoMarkerHolder;
      this.mapViewer = mapViewer;
   }

   @Override
   public void mouseReleased(MouseEvent e) {
   }

   @Override
   public void mousePressed(MouseEvent e) {
   }

   @Override
   public void mouseExited(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
         geoMarkerHolder.updateGeoMarker(new PointMarker(geoPosition));
      }
   }
}