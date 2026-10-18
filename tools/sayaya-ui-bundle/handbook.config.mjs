import base from './rollup.config.js';
import {nodeResolve} from '@rollup/plugin-node-resolve';
import terser from '@rollup/plugin-terser';
const cssPlugin = base.plugins.find(p => p && p.name === 'css-module-to-cssresult');
export default [
  { input: '@material/web/all.js',
    output: { file: '../handbook/material-web.iife.js', format: 'iife' },
    plugins: [cssPlugin, nodeResolve(), terser()] },
  { input: 'src/index.js',
    output: { file: '../handbook/labs.iife.js', format: 'iife', intro: base.output.intro, outro: base.output.outro },
    plugins: base.plugins }
];
