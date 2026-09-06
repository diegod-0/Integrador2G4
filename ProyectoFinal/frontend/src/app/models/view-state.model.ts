/**
 * Máquina de Estados discriminada para vistas reactivas (Signals)
 * Garantiza tratamiento determinista de estados sin variables booleanas dispersas.
 */
export type ViewState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'empty'; message: string }
  | { status: 'error'; message: string };
