"""Safety/format contracts for the native chest fixture (not gameplay tests)."""
import unittest
from unittest.mock import patch
import smoke_radiant_chest as fixture


class ChestFixtureTest(unittest.TestCase):
    def test_world_mutations_are_guarded_and_forceload_is_preserved(self):
        objective = 'aa_c_test'
        for node in fixture.NODES:
            sessions = fixture.fixtures(node, objective)
            for commands in sessions:
                for command in commands:
                    if any(token in command for token in ('setblock ', 'fill ', 'item replace ', 'data merge ', 'kill ', 'tag @e')):
                        self.assertTrue(command.startswith('execute if score ready aa_c_test matches 1 run '), command)
            self.assertIn('unless entity @e[', sessions[0][4])
            self.assertIn('if block 3 298 3 air', sessions[0][4])
            self.assertIn('if block 7 302 7 air', sessions[0][4])
            self.assertIn('execute if score forced aa_c_test matches 0 run forceload remove 0 0', sessions[1])
            self.assertTrue(any('setblock 5 299 5 air destroy' in command for command in sessions[1]))
            self.assertTrue(any('PickupDelay:32767s' in command for command in sessions[1]))

    def test_native_item_data_is_version_matched(self):
        for node in fixture.NODES:
            setup, restore = fixture.fixtures(node, 'aa_c_test')
            text = '\n'.join(setup + restore)
            if node.startswith('1.21'):
                self.assertIn('count:1,components:{"minecraft:custom_data":', text)
                self.assertIn('Item.count', text)
                self.assertNotIn('Item.Count', text)
            else:
                self.assertIn('Count:1b,tag:{aa_chest_probe:1b}', text)
                self.assertIn('Item.Count', text)
                self.assertNotIn('Item.count', text)

    def test_failed_setup_assertion_does_not_skip_cleanup_session(self):
        with patch.object(fixture, 'preflight'), patch.object(fixture, 'ROOT') as root, patch.object(fixture, 'smoke', side_effect=[False, True]) as smoke, patch('builtins.print'):
            root.__truediv__.return_value.__truediv__.return_value.__truediv__.return_value.exists.return_value = False
            self.assertFalse(fixture.run('1.21.1-fabric'))
            self.assertEqual(smoke.call_count, 2)
            self.assertEqual(smoke.call_args.kwargs['markers'][-1], 'AA_CHEST_RESTART_FINISHED')


if __name__ == '__main__':
    unittest.main()
