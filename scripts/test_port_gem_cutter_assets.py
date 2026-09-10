"""Regression checks for legacy ordered-rotation to native XYZ conversion."""
import math
import unittest

from port_gem_cutter_assets import rotation_xyz


def rotate(vector, axis, degrees):
    x, y, z = vector
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    if axis == 'x':
        return x, c * y - s * z, s * y + c * z
    if axis == 'y':
        return c * x + s * z, y, -s * x + c * z
    return c * x - s * y, s * x + c * y, z


class ItemTransformTest(unittest.TestCase):
    def test_release_perspective_rotations_preserve_each_basis_vector(self):
        for y, x, z in [(-75, 0, 0), (-70, 0, 0), (0, 30, 0), (45, 20, 20), (0, 0, 0),
                        (-90, 0, 90), (90, 0, 90), (-90, 0, -45), (90, 0, -45)]:
            euler = rotation_xyz([{'y': y}, {'x': x}, {'z': z}])
            for basis in [(1, 0, 0), (0, 1, 0), (0, 0, 1)]:
                old = rotate(rotate(rotate(basis, 'z', z), 'x', x), 'y', y)
                new = rotate(rotate(rotate(basis, 'z', euler[2]), 'y', euler[1]), 'x', euler[0])
                for expected, actual in zip(old, new):
                    self.assertAlmostEqual(expected, actual, places=12)

    def test_rotation_order_is_not_silently_relabelled(self):
        converted = rotation_xyz([{'y': 45}, {'x': 20}, {'z': 20}])
        self.assertNotEqual([20, 45, 20], converted)

    def test_monitoring_crystal_block_rotations_preserve_basis(self):
        from port_monitoring_crystal_assets import ROTATIONS
        legacy = {'up': (-90, 0), 'down': (90, 0), 'south': (0, 0),
                  'east': (0, 90), 'north': (0, 180), 'west': (0, 270)}
        for facing, (x, y) in legacy.items():
            nx, ny = ROTATIONS[facing]
            for basis in [(1, 0, 0), (0, 1, 0), (0, 0, 1)]:
                old = rotate(rotate(basis, 'x', x), 'y', y)
                new = rotate(rotate(basis, 'x', -nx), 'y', -ny)
                for expected, actual in zip(old, new):
                    self.assertAlmostEqual(expected, actual, places=12)


if __name__ == '__main__':
    unittest.main()
