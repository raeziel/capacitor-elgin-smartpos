export interface SmartPOSPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
