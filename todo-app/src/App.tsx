import { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Trash2, Plus, ListChecks } from "lucide-react";

interface Todo {
  id: number;
  text: string;
  completed: boolean;
}

type FilterType = "all" | "active" | "completed";

function App() {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [inputValue, setInputValue] = useState("");
  const [filter, setFilter] = useState<FilterType>("all");

  const addTodo = useCallback(() => {
    const trimmed = inputValue.trim();
    if (!trimmed) return;
    setTodos((prev) => [
      ...prev,
      { id: Date.now(), text: trimmed, completed: false },
    ]);
    setInputValue("");
  }, [inputValue]);

  const toggleTodo = useCallback((id: number) => {
    setTodos((prev) =>
      prev.map((todo) =>
        todo.id === id ? { ...todo, completed: !todo.completed } : todo
      )
    );
  }, []);

  const removeTodo = useCallback((id: number) => {
    setTodos((prev) => prev.filter((todo) => todo.id !== id));
  }, []);

  const clearCompleted = useCallback(() => {
    setTodos((prev) => prev.filter((todo) => !todo.completed));
  }, []);

  const filteredTodos = todos.filter((todo) => {
    if (filter === "active") return !todo.completed;
    if (filter === "completed") return todo.completed;
    return true;
  });

  const activeCount = todos.filter((t) => !t.completed).length;
  const completedCount = todos.filter((t) => t.completed).length;

  return (
    <div className="min-h-screen bg-zinc-50 flex items-start justify-center pt-16 px-4">
      <Card className="w-full max-w-lg shadow-lg">
        <CardHeader className="pb-4">
          <CardTitle className="flex items-center gap-2 text-2xl">
            <ListChecks className="h-6 w-6 text-zinc-700" />
            Todo App
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Add todo input */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              addTodo();
            }}
            className="flex gap-2"
          >
            <Input
              placeholder="What needs to be done?"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              className="flex-1"
            />
            <Button type="submit" disabled={!inputValue.trim()}>
              <Plus className="h-4 w-4 mr-1" />
              Add
            </Button>
          </form>

          {/* Filter buttons */}
          <div className="flex gap-1 border-b border-zinc-200 pb-3">
            {(["all", "active", "completed"] as FilterType[]).map((f) => (
              <Button
                key={f}
                variant={filter === f ? "default" : "ghost"}
                size="sm"
                onClick={() => setFilter(f)}
                className="capitalize"
              >
                {f}
                {f === "all" && ` (${todos.length})`}
                {f === "active" && ` (${activeCount})`}
                {f === "completed" && ` (${completedCount})`}
              </Button>
            ))}
          </div>

          {/* Todo list */}
          <div className="space-y-1">
            {filteredTodos.length === 0 && (
              <p className="text-center text-zinc-400 py-8 text-sm">
                {todos.length === 0
                  ? "No todos yet. Add one above!"
                  : "No todos match this filter."}
              </p>
            )}
            {filteredTodos.map((todo) => (
              <div
                key={todo.id}
                className="flex items-center gap-3 rounded-md px-3 py-2 hover:bg-zinc-100 group"
              >
                <Checkbox
                  checked={todo.completed}
                  onCheckedChange={() => toggleTodo(todo.id)}
                  id={`todo-${todo.id}`}
                />
                <label
                  htmlFor={`todo-${todo.id}`}
                  className={`flex-1 cursor-pointer select-none text-sm ${
                    todo.completed
                      ? "line-through text-zinc-400"
                      : "text-zinc-800"
                  }`}
                >
                  {todo.text}
                </label>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => removeTodo(todo.id)}
                  className="opacity-0 group-hover:opacity-100 transition-opacity h-7 w-7 p-0 text-zinc-400 hover:text-red-500"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </div>

          {/* Footer */}
          {todos.length > 0 && (
            <div className="flex items-center justify-between pt-2 border-t border-zinc-200 text-xs text-zinc-500">
              <span>
                {activeCount} item{activeCount !== 1 ? "s" : ""} left
              </span>
              {completedCount > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={clearCompleted}
                  className="text-xs h-7 text-zinc-400 hover:text-red-500"
                >
                  Clear completed
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

export default App;
