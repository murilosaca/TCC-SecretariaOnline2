export type HateoasLinks = Record<string, string | undefined>;

export type PageMeta = {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PageResponse<T> = {
  content: T[];
  page: PageMeta;
  _links?: HateoasLinks;
};
