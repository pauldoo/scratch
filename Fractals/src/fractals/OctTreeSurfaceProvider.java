/*
    Copyright (C) 2010  Paul Richards.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fractals;

import fractals.math.Triplex;
import java.awt.Color;
import java.util.Collection;

final class OctTreeSurfaceProvider implements RaytracerComponent.SurfaceProvider
{
    /**
        Callback interface used by OctTreeSurfaceProvider when
        the surface normal at a point is needed.

        This normal could be estimated using the structure of the OctTree
        segmentation, but in some cases the normal can be computed
        analytically by some other means.
    */
    public interface NormalProvider {
        /**
            Should calculate the surface normal for the
            given point (which will only roughly be on the surface).

            The returned value should be normalized.
        */
        Triplex normalAtPosition(Triplex p);
    }

    private final OctTree segmentation;
    private final NormalProvider normalProvider;

    public OctTreeSurfaceProvider(OctTree segmentation, NormalProvider normalProvider) {
        this.segmentation = segmentation;
        this.normalProvider = normalProvider;
    }

    @Override
    public Color colorAtPosition(Triplex position, Collection<Pair<Triplex, Color>> lights) {
        final Triplex normal = normalProvider.normalAtPosition(position);
        double red = 0.0;
        double blue = 0.0;
        double green = 0.0;
        for(Pair<Triplex, Color> light: lights) {
            final double shade = Math.max(Triplex.dotProduct(normal, light.first), 0.0);
            if (shade > 0.0) {
                red += shade * (light.second.getRed() / 255.0);
                green += shade * (light.second.getGreen() / 255.0);
                blue += shade * (light.second.getBlue() / 255.0);
            }
        }
        red = Math.min(red, 1.0);
        green = Math.min(green, 1.0);
        blue = Math.min(blue, 1.0);

        final Color color = new Color((float)(red), (float)(green), (float)(blue));
        return color;
    }

    @Override
    public double firstHit(double x, double y, double z, double dx, double dy, double dz) {
        return segmentation.firstHit(x, y, z, dx, dy, dz);
    }
}
