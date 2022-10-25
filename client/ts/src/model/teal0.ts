/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation, Christoph Reichenbach.  All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const tealTokens = {
  keywords: [
    'fun', 'var', 'while', 'if', 'else', 'return',
    'not', 'and', 'or', 'null', 'new',
    'for', 'in', 'qualifier', 'type',
    'assert',
    'class', 'self'
  ],

  typeKeywords: [
    'int', 'string', 'array', 'any', 'nonnull'
  ],

  operators: [
    '+', '-', '*', '/', '%',
    ':=',
    '>', '<', '==', '<=', '>=', '!=', '=',
    ':', '<:'
  ],

  brackets:  [ ['{','}','delimiter.curly'],
	       ['[',']','delimiter.square'],
	       ['(',')','delimiter.parenthesis'] ],

  // we include these common regular expressions
  symbols:  /[=><!~?:&|+\-*\/\^%]+/,

  escapes: /\\(?:[\\"])/,

  // The main tokenizer for our languages
  tokenizer: {
    root: [
      // identifiers and keywords
      [/[a-zA-Z_][a-zA-Z_0-9]*/, { cases: { '@typeKeywords': 'keyword',
					    '@keywords': 'keyword',
					    '@default': 'identifier' } }],
      [/[A-Z][a-zA-Z_0-9]*/, 'type.identifier' ],  // to show class names nicely

      // whitespace
      { include: '@whitespace' },

      // delimiters and operators
      [/[{}()\[\]]/, '@brackets'],
      [/@symbols/, { cases: { '@operators': 'operator',
                              '@default'  : '' } } ],

      // // @ annotations.
      // // As an example, we emit a debugging log message on these tokens.
      // // Note: message are supressed during the first load -- change some lines to see them.
      // [/@\s*[a-zA-Z_\$][\w\$]*/, { token: 'annotation', log: 'annotation token: $0' }],

      // numbers
      [/\d+/, 'number'],

      // delimiter: after number because of .\d floats
      [/[;,.]/, 'delimiter'],

      // strings
      [/"([^"\\]|\\.)*$/, 'string.invalid' ],  // non-teminated string
      [/"/,  { token: 'string.quote', bracket: '@open', next: '@string' } ],

      // characters
      [/'[^\\']'/, 'string'],
      [/(')(@escapes)(')/, ['string','string.escape','string']],
      [/'/, 'string.invalid']
    ],

    comment: [
      [/[^\/*]+/, 'comment'],
      [/\*\//, 'comment', '@pop'],
      [/[\/*]/, 'comment']
    ],

    string: [
      [/[^\\"]+/,  'string'],
      [/@escapes/, 'string.escape'],
      [/\\./,      'string.escape.invalid'],
      [/"/,        { token: 'string.quote', bracket: '@close', next: '@pop' } ]
    ],

    whitespace: [
      [/[ \t\r\n]+/, ''],
      [/\/\*/, 'comment', '@comment'],
      [/\/\/.*$/, 'comment']
    ],
  },
};

const tealConf = {
  comments: {
    lineComment: '//',
    blockComment: ['/*', '*/']
  },
  brackets: [
    ['{', '}'],
    ['[', ']'],
    ['(', ')']
  ],
}

export const teal0Init = (editorType: string) => {
  if (editorType == 'Monaco') {
    var languages = (window as any).monaco.languages;
    languages.register({ id : 'teal' });
    languages.setMonarchTokensProvider('teal', tealTokens);
    languages.setLanguageConfiguration('teal', tealConf);
  }
};

// import { languages } from '../../fillers/monaco-editor-core';

// export const conf = { //: languages.LanguageConfiguration = {
// 	comments: {
// 		lineComment: '#',
// 		blockComment: ["'''", "'''"]
// 	},
// 	brackets: [
// 		['{', '}'],
// 		['[', ']'],
// 		['(', ')']
// 	],
// 	autoClosingPairs: [
// 		{ open: '{', close: '}' },
// 		{ open: '[', close: ']' },
// 		{ open: '(', close: ')' },
// 		{ open: '"', close: '"', notIn: ['string'] },
// 		{ open: "'", close: "'", notIn: ['string', 'comment'] }
// 	],
// 	surroundingPairs: [
// 		{ open: '{', close: '}' },
// 		{ open: '[', close: ']' },
// 		{ open: '(', close: ')' },
// 		{ open: '"', close: '"' },
// 		{ open: "'", close: "'" }
// 	],
// 	onEnterRules: [
// 		{
// 			beforeText: new RegExp(
// 				'^\\s*(?:def|class|for|if|elif|else|while|try|with|finally|except|async|match|case).*?:\\s*$'
// 			),
// 			action: { indentAction: languages.IndentAction.Indent }
// 		}
// 	],
// 	folding: {
// 		offSide: true,
// 		markers: {
// 			start: new RegExp('^\\s*#region\\b'),
// 			end: new RegExp('^\\s*#endregion\\b')
// 		}
// 	}
// };

// export const language = <languages.IMonarchLanguage>{
// 	defaultToken: '',
// 	tokenPostfix: '.teal',

// 	keywords: [
// 		// This section is the result of running
// 		// `import keyword; for k in sorted(keyword.kwlist + keyword.softkwlist): print("  '" + k + "',")`
// 		// in a Python REPL,
// 		// though note that the output from Python 3 is not a strict superset of the
// 		// output from Python 2.
// 		'False', // promoted to keyword.kwlist in Python 3
// 		'None', // promoted to keyword.kwlist in Python 3
// 		'True', // promoted to keyword.kwlist in Python 3
// 		'_', // new in Python 3.10
// 		'and',
// 		'as',
// 		'assert',
// 		'async', // new in Python 3
// 		'await', // new in Python 3
// 		'break',
// 		'case', // new in Python 3.10
// 		'class',
// 		'continue',
// 		'def',
// 		'del',
// 		'elif',
// 		'else',
// 		'except',
// 		'exec', // Python 2, but not 3.
// 		'finally',
// 		'for',
// 		'from',
// 		'global',
// 		'if',
// 		'import',
// 		'in',
// 		'is',
// 		'lambda',
// 		'match', // new in Python 3.10
// 		'nonlocal', // new in Python 3
// 		'not',
// 		'or',
// 		'pass',
// 		'print', // Python 2, but not 3.
// 		'raise',
// 		'return',
// 		'try',
// 		'while',
// 		'with',
// 		'yield',

// 		'int',
// 		'float',
// 		'long',
// 		'complex',
// 		'hex',

// 		'abs',
// 		'all',
// 		'any',
// 		'apply',
// 		'basestring',
// 		'bin',
// 		'bool',
// 		'buffer',
// 		'bytearray',
// 		'callable',
// 		'chr',
// 		'classmethod',
// 		'cmp',
// 		'coerce',
// 		'compile',
// 		'complex',
// 		'delattr',
// 		'dict',
// 		'dir',
// 		'divmod',
// 		'enumerate',
// 		'eval',
// 		'execfile',
// 		'file',
// 		'filter',
// 		'format',
// 		'frozenset',
// 		'getattr',
// 		'globals',
// 		'hasattr',
// 		'hash',
// 		'help',
// 		'id',
// 		'input',
// 		'intern',
// 		'isinstance',
// 		'issubclass',
// 		'iter',
// 		'len',
// 		'locals',
// 		'list',
// 		'map',
// 		'max',
// 		'memoryview',
// 		'min',
// 		'next',
// 		'object',
// 		'oct',
// 		'open',
// 		'ord',
// 		'pow',
// 		'print',
// 		'property',
// 		'reversed',
// 		'range',
// 		'raw_input',
// 		'reduce',
// 		'reload',
// 		'repr',
// 		'reversed',
// 		'round',
// 		'self',
// 		'set',
// 		'setattr',
// 		'slice',
// 		'sorted',
// 		'staticmethod',
// 		'str',
// 		'sum',
// 		'super',
// 		'tuple',
// 		'type',
// 		'unichr',
// 		'unicode',
// 		'vars',
// 		'xrange',
// 		'zip',

// 		'__dict__',
// 		'__methods__',
// 		'__members__',
// 		'__class__',
// 		'__bases__',
// 		'__name__',
// 		'__mro__',
// 		'__subclasses__',
// 		'__init__',
// 		'__import__'
// 	],

// 	brackets: [
// 		{ open: '{', close: '}', token: 'delimiter.curly' },
// 		{ open: '[', close: ']', token: 'delimiter.bracket' },
// 		{ open: '(', close: ')', token: 'delimiter.parenthesis' }
// 	],

// 	tokenizer: {
// 		root: [
// 			{ include: '@whitespace' },
// 			{ include: '@numbers' },
// 			{ include: '@strings' },

// 			[/[,:;]/, 'delimiter'],
// 			[/[{}\[\]()]/, '@brackets'],

// 			[/@[a-zA-Z_]\w*/, 'tag'],
// 			[
// 				/[a-zA-Z_]\w*/,
// 				{
// 					cases: {
// 						'@keywords': 'keyword',
// 						'@default': 'identifier'
// 					}
// 				}
// 			]
// 		],

// 		// Deal with white space, including single and multi-line comments
// 		whitespace: [
// 			[/\s+/, 'white'],
// 			[/(^#.*$)/, 'comment'],
// 			[/'''/, 'string', '@endDocString'],
// 			[/"""/, 'string', '@endDblDocString']
// 		],
// 		endDocString: [
// 			[/[^']+/, 'string'],
// 			[/\\'/, 'string'],
// 			[/'''/, 'string', '@popall'],
// 			[/'/, 'string']
// 		],
// 		endDblDocString: [
// 			[/[^"]+/, 'string'],
// 			[/\\"/, 'string'],
// 			[/"""/, 'string', '@popall'],
// 			[/"/, 'string']
// 		],

// 		// Recognize hex, negatives, decimals, imaginaries, longs, and scientific notation
// 		numbers: [
// 			[/-?0x([abcdef]|[ABCDEF]|\d)+[lL]?/, 'number.hex'],
// 			[/-?(\d*\.)?\d+([eE][+\-]?\d+)?[jJ]?[lL]?/, 'number']
// 		],

// 		// Recognize strings, including those broken across lines with \ (but not without)
// 		strings: [
// 			[/'$/, 'string.escape', '@popall'],
// 			[/'/, 'string.escape', '@stringBody'],
// 			[/"$/, 'string.escape', '@popall'],
// 			[/"/, 'string.escape', '@dblStringBody']
// 		],
// 		stringBody: [
// 			[/[^\\']+$/, 'string', '@popall'],
// 			[/[^\\']+/, 'string'],
// 			[/\\./, 'string'],
// 			[/'/, 'string.escape', '@popall'],
// 			[/\\$/, 'string']
// 		],
// 		dblStringBody: [
// 			[/[^\\"]+$/, 'string', '@popall'],
// 			[/[^\\"]+/, 'string'],
// 			[/\\./, 'string'],
// 			[/"/, 'string.escape', '@popall'],
// 			[/\\$/, 'string']
// 		]
// 	}
// };
