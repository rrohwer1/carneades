;;;
;; Note during the movement to R6RS none of these routines are required but
;; were in the original source files.  If sxml stuff does not use it we
;; can delete this file.

(library
  (ssax common)
  
  (export)
  
  (import (rnrs base)
          (rnrs io simple))
  
  ;;(define (command-line)
  ;;  (cons "plt" (vector->list (current-command-line-arguments)
                                        ;       argv
  ;;                            )))
  
                                        ;(define (call-with-input-string str fun)
                                        ;   (fun (open-input-string str)))
                                        ;
                                        ;(define (call-with-output-string fun)
                                        ;  (let ((outs (open-output-string)))
                                        ;  (fun outs)
                                        ;  (get-output-string outs)))
  
  ;; (define close-output-string get-output-string)
  
                                        ;
  (define (filter pred lis)
    (let rpt ((l lis))
      (if (null? l)
          '()
          (if (pred (car l))
              (cons (car l) (rpt (cdr l)))
              (rpt (cdr l))))))
  
  (define-syntax and-let*
    (syntax-rules ()
      ((and-let* () body ...)
       (begin body ...))
      ((and-let* ((var expr) clauses ...) body ...)
       (let ((var expr))
         (if var (and-let* (clauses ...) body ...) #f)))
      ((and-let* ((expr) clauses ...) body ...)
       (if expr (and-let* (clauses ...) body ...) #f))
      ((and-let* (var clauses ...) body ...)
       (if var (and-let* (clauses ...) body ...) #f)))))
