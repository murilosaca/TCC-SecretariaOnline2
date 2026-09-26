import * as DocumentPicker from 'expo-document-picker';

export type NativeUploadFile = {
  uri: string;
  name: string;
  mimeType: string;
};

/** Seleciona um PDF para upload multipart nativo. */
export async function escolherPdf(): Promise<NativeUploadFile | null> {
  const result = await DocumentPicker.getDocumentAsync({
    type: 'application/pdf',
    copyToCacheDirectory: true,
    multiple: false,
  });
  if (result.canceled || !result.assets?.[0]) {
    return null;
  }
  const asset = result.assets[0];
  return {
    uri: asset.uri,
    name: asset.name || 'documento.pdf',
    mimeType: asset.mimeType || 'application/pdf',
  };
}
