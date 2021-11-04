import { registerPlugin } from '@capacitor/core';

import type { SmartPOSPlugin } from './definitions';

const SmartPOS = registerPlugin<SmartPOSPlugin>('SmartPOS', {
  web: () => import('./web').then(m => new m.SmartPOSWeb()),
});

export * from './definitions';
export { SmartPOS };
