/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.strongback.Logger;
import org.strongback.command.Scheduler.CommandListener;

/**
 * @author Randall Hauch
 */
public class CommandRunnerTest {

    private static final long INITIAL_TIME = 5001;
    private static final CommandRunner.Context CONTEXT = new CommandRunner.Context() {
        @Override
        public CommandListener listener() {
            return CommandListener.noOp();
        }

        @Override
        public Logger logger() {
            return Logger.noOp();
        }
    };

    @Test
    public void shouldRunCommandWithTimeout() {
        WatchedCommand watched = WatchedCommand.watch(Command.pause(1000, TimeUnit.MILLISECONDS));
        CommandRunner runner = new CommandRunner(watched);
        assertFalse(runner.step(INITIAL_TIME));
        assertEquals(runner.state(), CommandState.RUNNING);
        assertIncomplete(watched);
        assertFalse(runner.step(INITIAL_TIME + 999));
        assertEquals(runner.state(), CommandState.RUNNING);
        assertIncomplete(watched);
        assertTrue(runner.step(INITIAL_TIME + 1000));
        assertEquals(runner.state(), CommandState.FINALIZED);
        assertComplete(watched);
    }

    @Test
    public void shouldInterruptCommandThatThrowsExceptionDuringInitialize() {
        WatchedCommand watched = WatchedCommand.watch(new Command() {
            @Override
            public void initialize() {
                throw new IllegalStateException();
            }

            @Override
            public boolean execute() {
                return false;
            }
        });
        CommandRunner runner = new CommandRunner(CONTEXT, watched);
        assertTrue(runner.step(INITIAL_TIME)); // completes because it is interrupted
        assertInterrupted(watched);
    }

    @Test
    public void shouldInterruptCommandThatThrowsExceptionDuringFirstExecute() {
        WatchedCommand watched = WatchedCommand.watch(Command.create((Runnable) () -> {
            throw new IllegalStateException();
        }));
        CommandRunner runner = new CommandRunner(CONTEXT, watched);
        assertTrue(runner.step(INITIAL_TIME)); // completes because it is interrupted
        assertExecutedAtLeast(watched, 1);
        assertInterrupted(watched);
    }

    @Test
    public void shouldInterruptCommandThatThrowsExceptionDuringSecondExecute() {
        WatchedCommand watched = WatchedCommand.watch(new Command() {
            private boolean once = false;

            @Override
            public boolean execute() {
                if (once)
                    throw new IllegalStateException();
                once = true;
                return false;
            }
        });
        CommandRunner runner = new CommandRunner(CONTEXT, watched);
        assertFalse(runner.step(INITIAL_TIME)); // executed correctly the first time
        assertExecutedAtLeast(watched, 1);
        assertTrue(runner.step(INITIAL_TIME)); // completes because it is interrupted
        assertExecutedAtLeast(watched, 2);
        assertInterrupted(watched);
    }

    protected void assertIncomplete(WatchedCommand watched) {
        assertTrue(watched.isInitialized());
        assertTrue(watched.isExecuted());
        assertFalse(watched.isEnded());
        assertFalse(watched.isInterrupted());
    }

    protected void assertComplete(WatchedCommand watched) {
        assertTrue(watched.isInitialized());
        assertTrue(watched.isExecuted());
        assertTrue(watched.isEnded());
        assertFalse(watched.isInterrupted());
    }

    protected void assertInterrupted(WatchedCommand watched) {
        assertTrue(watched.isInterrupted());
        assertFalse(watched.isEnded());
    }

    protected void assertExecutedAtLeast(WatchedCommand watched, int minimum) {
        assertTrue(watched.isInitialized());
        assertTrue(watched.isExecutedAtLeast(minimum));
    }

}
