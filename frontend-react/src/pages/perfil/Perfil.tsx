import { FormEvent, useEffect, useMemo, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { perfilApi } from '../../api/perfil'
import { useActions } from '../../hooks/useActions'
import { IDENTIDADES_GENERO, type Perfil as PerfilDto, type PerfilPatch } from '../../models/perfil'
import { PerfilNav } from './PerfilNav'

const MAX_FOTO = 2 * 1024 * 1024

type Formulario = {
  nomeSocial: string
  telefone: string
  emailPessoal: string
  identidadeGenero: string
}

const VAZIO: Formulario = {
  nomeSocial: '',
  telefone: '',
  emailPessoal: '',
  identidadeGenero: '',
}

function dePerfil(perfil: PerfilDto): Formulario {
  return {
    nomeSocial: perfil.nomeSocial ?? '',
    telefone: perfil.telefone ?? '',
    emailPessoal: perfil.emailPessoal ?? '',
    identidadeGenero: perfil.identidadeGenero ?? '',
  }
}

function payload(form: Formulario, base: Formulario): PerfilPatch {
  const body: PerfilPatch = {}
  if (form.nomeSocial !== base.nomeSocial) {
    body.nomeSocial = form.nomeSocial.trim() || null
  }
  if (form.telefone !== base.telefone) {
    body.telefone = form.telefone.trim() || null
  }
  if (form.emailPessoal !== base.emailPessoal) {
    body.emailPessoal = form.emailPessoal.trim() || null
  }
  if (form.identidadeGenero !== base.identidadeGenero) {
    body.identidadeGenero = form.identidadeGenero || null
  }
  return body
}

function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) {
    return '?'
  }
  if (partes.length === 1) {
    return partes[0].slice(0, 2).toUpperCase()
  }
  return `${partes[0][0]}${partes[partes.length - 1][0]}`.toUpperCase()
}

async function recortarCentro(file: File): Promise<Blob> {
  const bitmap = await createImageBitmap(file)
  const lado = Math.min(bitmap.width, bitmap.height)
  const sx = Math.floor((bitmap.width - lado) / 2)
  const sy = Math.floor((bitmap.height - lado) / 2)
  const canvas = document.createElement('canvas')
  canvas.width = 512
  canvas.height = 512
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    throw new Error('crop')
  }
  ctx.drawImage(bitmap, sx, sy, lado, lado, 0, 0, 512, 512)
  const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.9))
  if (!blob) {
    throw new Error('crop')
  }
  return blob
}

export function Perfil() {
  const queryClient = useQueryClient()
  const consulta = useQuery({ queryKey: ['perfil'], queryFn: perfilApi.obter })
  const actions = useActions(consulta.data?._links)
  const [form, setForm] = useState<Formulario>(VAZIO)
  const [base, setBase] = useState<Formulario>(VAZIO)
  const [toast, setToast] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [preview, setPreview] = useState<string | null>(null)
  const [arquivo, setArquivo] = useState<File | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!consulta.data) {
      return
    }
    const atual = dePerfil(consulta.data)
    setForm(atual)
    setBase(atual)
  }, [consulta.data])

  const dirty = useMemo(
    () =>
      form.nomeSocial !== base.nomeSocial ||
      form.telefone !== base.telefone ||
      form.emailPessoal !== base.emailPessoal ||
      form.identidadeGenero !== base.identidadeGenero,
    [form, base],
  )

  const salvar = useMutation({
    mutationFn: () => {
      const href = actions.href('update')
      if (!href) {
        throw new Error('Sem _links.update')
      }
      return perfilApi.atualizar(href, payload(form, base))
    },
    onSuccess: (perfil) => {
      queryClient.setQueryData(['perfil'], perfil)
      setToast('Perfil atualizado com sucesso.')
      setErro(null)
    },
    onError: (error) => {
      setToast(null)
      setErro(error instanceof ApiError ? error.message : 'Não foi possível salvar o perfil.')
    },
  })

  const foto = useMutation({
    mutationFn: async (file: File) => {
      const href = actions.href('foto')
      if (!href) {
        throw new Error('Sem _links.foto')
      }
      const blob = await recortarCentro(file)
      const dados = new FormData()
      dados.append('arquivo', blob, 'foto.jpg')
      return perfilApi.enviarFoto(href, dados)
    },
    onSuccess: (perfil) => {
      queryClient.setQueryData(['perfil'], perfil)
      setToast('Perfil atualizado com sucesso.')
      setErro(null)
      fecharCrop()
    },
    onError: (error) => {
      setErro(error instanceof ApiError ? error.message : 'Não foi possível enviar a foto.')
      fecharCrop()
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!actions.can('update') || !dirty || salvar.isPending) {
      return
    }
    setToast(null)
    salvar.mutate()
  }

  function cancelar() {
    setForm(base)
    setToast(null)
    setErro(null)
  }

  function selecionarFoto(file: File | undefined) {
    setErro(null)
    if (!file) {
      return
    }
    if (file.size > MAX_FOTO) {
      setErro('A imagem deve ter no máximo 2 MB.')
      return
    }
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      setErro('Use JPEG, PNG ou WebP.')
      return
    }
    setArquivo(file)
    setPreview(URL.createObjectURL(file))
  }

  function fecharCrop() {
    if (preview) {
      URL.revokeObjectURL(preview)
    }
    setPreview(null)
    setArquivo(null)
    if (inputRef.current) {
      inputRef.current.value = ''
    }
  }

  if (consulta.isLoading) {
    return (
      <section className="page" aria-busy="true">
        <h1>Perfil</h1>
        <p className="muted">Carregando perfil…</p>
      </section>
    )
  }

  if (consulta.isError || !consulta.data) {
    const message = consulta.error instanceof ApiError ? consulta.error.message : 'Não foi possível carregar o perfil.'
    return (
      <section className="page">
        <h1>Perfil</h1>
        <PerfilNav />
        <div className="banner danger" role="alert">
          {message}
        </div>
      </section>
    )
  }

  const perfil = consulta.data

  return (
    <section className="page">
      <h1>Perfil</h1>
      <p className="lead">Dados pessoais. GRR e e-mail institucional são somente leitura.</p>
      <PerfilNav />
      {toast && (
        <div className="banner success" role="status">
          {toast}
        </div>
      )}
      {erro && (
        <div className="banner danger" role="alert">
          {erro}
        </div>
      )}
      <form className="perfil-layout" onSubmit={onSubmit} noValidate>
        <div className="perfil-foto">
          {perfil.fotoUrl ? (
            <img className="avatar" src={perfil.fotoUrl} alt={`Foto de perfil de ${perfil.nome}`} />
          ) : (
            <div className="avatar avatar-fallback" aria-hidden="true">
              {iniciais(perfil.nome)}
            </div>
          )}
          {actions.can('foto') && (
            <>
              <button type="button" className="ghost" onClick={() => inputRef.current?.click()}>
                Alterar foto
              </button>
              <input
                ref={inputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                hidden
                aria-label="Selecionar foto"
                onChange={(event) => selecionarFoto(event.target.files?.[0])}
              />
            </>
          )}
        </div>
        <div>
        <div className="grid">
          <label>
            Nome
            <input value={perfil.nome} disabled readOnly />
          </label>
          <label>
            GRR
            <input value={perfil.grr ?? ''} disabled readOnly />
          </label>
          <label>
            E-mail institucional
            <input value={perfil.emailInstitucional} disabled readOnly />
          </label>
          <label>
            Nome social
            <input
              value={form.nomeSocial}
              onChange={(event) => setForm({ ...form, nomeSocial: event.target.value })}
              maxLength={120}
            />
          </label>
          <label>
            Telefone
            <input
              value={form.telefone}
              inputMode="tel"
              onChange={(event) => setForm({ ...form, telefone: event.target.value })}
              maxLength={20}
            />
          </label>
          <label>
            E-mail pessoal
            <input
              type="email"
              value={form.emailPessoal}
              onChange={(event) => setForm({ ...form, emailPessoal: event.target.value })}
            />
          </label>
          <label>
            Identidade de gênero
            <select
              value={form.identidadeGenero}
              onChange={(event) => setForm({ ...form, identidadeGenero: event.target.value })}
            >
              {IDENTIDADES_GENERO.map((opcao) => (
                <option key={opcao.value || 'vazio'} value={opcao.value}>
                  {opcao.label}
                </option>
              ))}
            </select>
          </label>
        </div>
        {actions.can('update') && (
          <div className="form-footer">
            <button type="button" className="ghost" onClick={cancelar}>
              Cancelar
            </button>
            <button type="submit" disabled={!dirty || salvar.isPending}>
              {salvar.isPending ? 'Salvando…' : 'Salvar'}
            </button>
          </div>
        )}
        </div>
      </form>
      {preview && (
        <div className="modal-backdrop" role="presentation">
          <div className="panel" role="dialog" aria-modal="true" aria-labelledby="crop-titulo">
            <h2 id="crop-titulo">Ajustar foto</h2>
            <img className="avatar" src={preview} alt="Pré-visualização do recorte circular" />
            <div className="actions">
              <button type="button" className="ghost" onClick={fecharCrop}>
                Cancelar
              </button>
              <button
                type="button"
                disabled={foto.isPending || !arquivo}
                onClick={() => arquivo && foto.mutate(arquivo)}
              >
                {foto.isPending ? 'Enviando…' : 'Confirmar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
