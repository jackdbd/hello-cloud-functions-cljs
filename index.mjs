import functions from '@google-cloud/functions-framework'
import { loadFile } from 'nbb'

const { hello } = await loadFile('./hello.cljs')

functions.http('hello', async (req, res) => {
  return hello(req, res)
})
