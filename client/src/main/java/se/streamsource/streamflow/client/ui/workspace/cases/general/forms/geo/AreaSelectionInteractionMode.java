/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import se.streamsource.streamflow.client.util.i18n;

public class AreaSelectionInteractionMode
   implements MapInteractionMode, MouseInputListener {

   private List<GeoPosition> points;
   private AreaPainter areaPainter;
   private JXMapViewer mapViewer;
   private GeoMarkerHolder geoMarkerHolder;

   @Override
   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder) {
      mapViewer.addMouseListener(this);
      mapViewer.addMouseMotionListener(this);

      points = new ArrayList<GeoPosition>();
      areaPainter = new AreaPainter();
      areaPainter.setPoints(points);
      mapViewer.setOverlayPainter(areaPainter);

      this.mapViewer = mapViewer;
      this.geoMarkerHolder = geoMarkerHolder;
   }

   @Override
   public void leaveMode(JXMapViewer mapViewer) {
      mapViewer.removeMouseListener(this);
      mapViewer.removeMouseMotionListener(this);
      mapViewer.setOverlayPainter(null);
   }

   @Override
   public String getHelpHint() {
      return i18n.text(GeoLocationFieldPanelResources.mode_hint_select_area);
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
      points.add(geoPosition);
      areaPainter.setPoints(points);
      mapViewer.repaint();

      if (e.getClickCount() == 2) {
         points.add(points.get(0)); // Close polygon
         geoMarkerHolder.updateGeoMarker(new PolygonMarker(points));
      }
   }

   @Override
   public void mousePressed(MouseEvent e) {
   }

   @Override
   public void mouseReleased(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   @Override
   public void mouseExited(MouseEvent e) {
   }

   @Override
   public void mouseDragged(MouseEvent e) {
   }

   @Override
   public void mouseMoved(MouseEvent e) {
      GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
      List<GeoPosition> pointsWithMousePoint = new ArrayList<GeoPosition>(points);
      pointsWithMousePoint.add(geoPosition);
      areaPainter.setPoints(pointsWithMousePoint);
      mapViewer.repaint();
   }
}
