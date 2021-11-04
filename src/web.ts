import { WebPlugin } from '@capacitor/core';

import type { SmartPOSPlugin } from './definitions';

export class SmartPOSWeb extends WebPlugin implements SmartPOSPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
